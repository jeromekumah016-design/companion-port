package com.companionport.extractor;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;

import java.util.Collections;

public class ChatExtractorService extends AccessibilityService {

    /**
     * Companion apps the user has chosen. Empty until the package names are confirmed
     * and a picker exists, so the service reads nothing from any app yet.
     */
    private final PackageGate gate = new PackageGate(Collections.emptySet());

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || !gate.allows(event.getPackageName())) {
            return; // not a chosen companion app: never read it
        }
        // Extraction of chat messages from allowed apps goes here.
    }

    @Override
    public void onInterrupt() {
        // Required method
    }
}
