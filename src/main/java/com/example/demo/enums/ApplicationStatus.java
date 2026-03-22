package com.example.demo.enums;

public enum ApplicationStatus {
    APPLIED,          // student applied
    REJECTED,         // admin rejected
    ACCEPTED,         // admin accepted

    TEST_PENDING,     // test available but not attempted
    TEST_SUBMITTED,   // student submitted, waiting evaluation

    PASSED,           // test passed
    FAILED,           // test failed

    SELECTED,         // final selection
    COMPLETED         // certificate done
}