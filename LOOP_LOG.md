# LOOP_LOG

## 2026-11-17 - [loop pass, run 22] make the skeleton build-shaped; limit what the service can read
- Repo check: `git rev-parse --show-toplevel` = /home/user/companion-port, branch claude/busy-dijkstra-dml4jx.
- Found (11-file Android skeleton; nothing could build):
  - `activity_main.xml` was committed under `src/main/java/.../android/app/src/main/res/layout/`, so `R.layout.activity_main` did not exist;
  - the strings file was named `strings.xml XML`, so `@string/app_name` was undefined;
  - `MainActivity` was not declared in the manifest: no launcher, the app could not be opened;
  - no Gradle repositories, no AppCompat dependency, no root build file;
  - the accessibility service could read every app's screen (no package restriction), asked for gesture control it never uses, had a literal (non-resource) description, and the app allowed cloud backup of what it reads.
- Fixed: files moved/renamed into `res/`; launcher activity declared; `allowBackup="false"`; gestures removed; description is a string resource; `PackageGate` (pure Java) drops events from every app not explicitly chosen, and can never allow system UI, Settings, the keyboard or itself. The chosen list is empty until real companion-app package names are confirmed, so the service reads nothing yet. Gradle: settings repositories, root plugin block (AGP 8.5.2), AppCompat + JUnit deps.
- Verify: `python3 scripts/check_android_tree.py` -> ok (the same script reports 9 problems on the original tree); `PackageGateTest` 8/8 passing with JUnit 4.13.2 on the JDK.
- NOT verified: an actual Gradle/Android build (no Android SDK in this environment), the AGP version choice, and anything on a device.
- Next (needs owner input): which companion apps to support, with confirmed package names; then add them to `android:packageNames` in `res/xml/accessibilityservice.xml` and the gate, and implement extraction.
