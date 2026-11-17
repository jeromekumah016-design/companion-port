#!/usr/bin/env python3
"""Structural checks for the Android project that do not need the Android SDK.

Catches the mistakes that kept this project from building: resources outside res/,
resource files without a .xml extension, unresolved @string references, an
Activity used in code but missing from the manifest, and over-broad accessibility
settings. Exit code 1 on any problem.
"""

import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent / "android" / "app" / "src" / "main"
ANDROID = "{http://schemas.android.com/apk/res/android}"


def main() -> int:
    problems: list[str] = []
    res = ROOT / "res"

    for path in ROOT.rglob("*"):
        if path.is_file() and "/res/" in path.as_posix() and not path.as_posix().startswith(res.as_posix()):
            problems.append(f"resource outside res/: {path.relative_to(ROOT)}")
    for path in res.rglob("*"):
        if path.is_file() and path.suffix != ".xml" and path.parent.name.startswith(("values", "layout", "xml")):
            problems.append(f"resource file without .xml extension: {path.relative_to(ROOT)}")

    strings: set[str] = set()
    for f in res.glob("values*/*.xml"):
        for el in ET.parse(f).getroot().iter("string"):
            strings.add(el.get("name"))
    for f in list(res.rglob("*.xml")) + [ROOT / "AndroidManifest.xml"]:
        for name in re.findall(r"@string/([A-Za-z0-9_]+)", f.read_text(encoding="utf-8")):
            if name not in strings:
                problems.append(f"{f.relative_to(ROOT)}: @string/{name} is not defined")
    for f in ROOT.rglob("*.java"):
        for name in re.findall(r"R\.string\.([A-Za-z0-9_]+)", f.read_text(encoding="utf-8")):
            if name not in strings:
                problems.append(f"{f.relative_to(ROOT)}: R.string.{name} is not defined")
        for name in re.findall(r"R\.layout\.([A-Za-z0-9_]+)", f.read_text(encoding="utf-8")):
            if not (res / "layout" / f"{name}.xml").exists():
                problems.append(f"{f.relative_to(ROOT)}: R.layout.{name} has no res/layout/{name}.xml")

    manifest = ET.parse(ROOT / "AndroidManifest.xml").getroot()
    app = manifest.find("application")
    activities = {a.get(f"{ANDROID}name") for a in app.findall("activity")}
    for f in ROOT.rglob("*.java"):
        text = f.read_text(encoding="utf-8")
        if re.search(r"class\s+(\w+)\s+extends\s+\w*Activity\b", text):
            cls = re.search(r"class\s+(\w+)", text).group(1)
            if f".{cls}" not in activities and cls not in activities:
                problems.append(f"{cls} extends an Activity but is not declared in the manifest")
    launchers = [a for a in app.findall("activity")
                 if any(c.get(f"{ANDROID}name") == "android.intent.category.LAUNCHER" for c in a.iter("category"))]
    if not launchers:
        problems.append("no LAUNCHER activity: the app cannot be opened")
    if app.get(f"{ANDROID}allowBackup") != "false":
        problems.append("allowBackup must be false: the service handles private conversations")

    svc = ET.parse(res / "xml" / "accessibilityservice.xml").getroot()
    if svc.get(f"{ANDROID}canPerformGestures") == "true":
        problems.append("accessibility service asks for gestures it does not use")
    desc = svc.get(f"{ANDROID}description") or ""
    if not desc.startswith("@string/"):
        problems.append("accessibility description must be a @string resource")

    for p in problems:
        print("PROBLEM:", p)
    print("ok" if not problems else f"{len(problems)} problem(s)")
    return 1 if problems else 0


if __name__ == "__main__":
    sys.exit(main())
