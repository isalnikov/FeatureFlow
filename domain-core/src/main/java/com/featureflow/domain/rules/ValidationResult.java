package com.featureflow.domain.rules;

public record ValidationResult(
    boolean isValid,
    String errorMessage
) {
    public static ValidationResult ok() {
        return new ValidationResult(true, null);
    }

    public static ValidationResult failure(String errorMessage) {
        return new ValidationResult(false, errorMessage);
    }
}
