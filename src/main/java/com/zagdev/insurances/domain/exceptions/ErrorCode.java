package com.zagdev.insurances.domain.exceptions;

public enum ErrorCode {

    UNEXPECTED_ERROR    (100, "Unexpected Error", "An unexpected error has occurred, please try again."),
    INVALID_DATA        (101, "Invalid data", "The data provided is invalid for this operation."),
    POLICY_NOT_FOUND    (102, "Data not found", "Policy not found."),
    INVALID_STATUS      (103, "Invalid policy status", "Policy is not allowed to does this operation.");

    private final Integer code;
    private final String title;
    private final String message;

    ErrorCode(Integer code, String title, String message) {
        this.code = code;
        this.title = title;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }
}
