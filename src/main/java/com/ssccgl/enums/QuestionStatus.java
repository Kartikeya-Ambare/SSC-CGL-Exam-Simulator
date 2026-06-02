package com.ssccgl.enums;

public enum QuestionStatus {
    NOT_VISITED,       // Grey  - never opened
    NOT_ANSWERED,      // Red   - visited but no answer
    ANSWERED,          // Green - answered
    MARKED_FOR_REVIEW, // Purple - marked, no answer
    ANSWERED_MARKED    // Purple+Green - answered AND marked
}
