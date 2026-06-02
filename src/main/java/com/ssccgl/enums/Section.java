package com.ssccgl.enums;

public enum Section {
    GENERAL_INTELLIGENCE_REASONING("General Intelligence & Reasoning"),
    GENERAL_AWARENESS("General Awareness"),
    QUANTITATIVE_APTITUDE("Quantitative Aptitude"),
    ENGLISH_COMPREHENSION("English Comprehension");

    private final String displayName;

    Section(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
