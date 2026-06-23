package com.companionport.extractor;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;

public class ChatExtractorService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // This is where we'll add code to read and extract chat messages
    }

    @Override
    public void onInterrupt() {
        // Required method
    }
}
