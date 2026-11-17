package com.companionport.extractor;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Decides which apps' screens the accessibility service may read.
 *
 * An accessibility service with canRetrieveWindowContent can read every app on the
 * phone: banking, password managers, other people's messages. This gate keeps the
 * service to the companion apps the user explicitly chose and ignores everything
 * else, including this app itself and the system UI.
 *
 * Pure Java (no Android imports) so it can be unit-tested without a device.
 */
public final class PackageGate {

    /** Never readable, even if someone adds them to the allow list by mistake. */
    private static final Set<String> NEVER = Set.of(
            "com.android.systemui",
            "com.android.settings",
            "com.google.android.inputmethod.latin",
            "com.companionport.extractor");

    private final Set<String> allowed;

    public PackageGate(Set<String> allowedPackages) {
        Set<String> copy = new LinkedHashSet<>();
        if (allowedPackages != null) {
            for (String p : allowedPackages) {
                String name = normalize(p);
                if (name != null && !NEVER.contains(name)) copy.add(name);
            }
        }
        this.allowed = Collections.unmodifiableSet(copy);
    }

    /** True only for an exact, allowed package name. Unknown or missing names are refused. */
    public boolean allows(CharSequence packageName) {
        if (packageName == null) return false;
        String name = normalize(packageName.toString());
        return name != null && allowed.contains(name);
    }

    public Set<String> allowed() {
        return allowed;
    }

    /** Package names are lowercase-insensitive dotted identifiers; anything else is rejected. */
    static String normalize(String raw) {
        if (raw == null) return null;
        String s = raw.trim();
        if (s.isEmpty() || s.length() > 255) return null;
        if (!s.matches("[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z][A-Za-z0-9_]*)+")) return null;
        return s;
    }
}
