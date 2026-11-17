package com.companionport.extractor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Set;
import org.junit.Test;

public class PackageGateTest {

    private static Set<String> set(String... s) {
        return new HashSet<>(Arrays.asList(s));
    }

    @Test
    public void emptyGateReadsNothing() {
        PackageGate gate = new PackageGate(Collections.emptySet());
        assertFalse(gate.allows("com.example.companion"));
        assertFalse(gate.allows("com.chase.sig.android"));
        assertTrue(gate.allowed().isEmpty());
    }

    @Test
    public void onlyChosenAppsAreReadable() {
        PackageGate gate = new PackageGate(set("com.example.companion"));
        assertTrue(gate.allows("com.example.companion"));
        assertFalse(gate.allows("com.example.companionx"));
        assertFalse(gate.allows("com.example"));
        assertFalse(gate.allows("com.android.chrome"));
    }

    @Test
    public void missingOrMalformedNamesAreRefused() {
        PackageGate gate = new PackageGate(set("com.example.companion"));
        assertFalse(gate.allows(null));
        assertFalse(gate.allows(""));
        assertFalse(gate.allows("   "));
        assertFalse(gate.allows("com.example.companion; rm -rf /"));
        assertFalse(gate.allows("companion"));
    }

    @Test
    public void surroundingWhitespaceIsIgnored() {
        PackageGate gate = new PackageGate(set("  com.example.companion "));
        assertTrue(gate.allows("com.example.companion"));
        assertTrue(gate.allows(" com.example.companion"));
    }

    @Test
    public void systemUiSettingsKeyboardAndSelfCanNeverBeAllowed() {
        PackageGate gate = new PackageGate(set(
                "com.android.systemui",
                "com.android.settings",
                "com.google.android.inputmethod.latin",
                "com.companionport.extractor",
                "com.example.companion"));
        assertEquals(set("com.example.companion"), gate.allowed());
        assertFalse(gate.allows("com.android.systemui"));
        assertFalse(gate.allows("com.google.android.inputmethod.latin"));
    }

    @Test
    public void invalidEntriesInTheAllowListAreDropped() {
        PackageGate gate = new PackageGate(set("not a package", "", "com.ok.app", "1com.bad.start"));
        assertEquals(set("com.ok.app"), gate.allowed());
    }

    @Test
    public void nullAllowListIsTreatedAsEmpty() {
        assertTrue(new PackageGate(null).allowed().isEmpty());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void theAllowListCannotBeChangedAfterConstruction() {
        new PackageGate(set("com.ok.app")).allowed().add("com.android.chrome");
    }
}
