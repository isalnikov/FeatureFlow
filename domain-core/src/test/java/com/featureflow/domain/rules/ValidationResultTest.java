package com.featureflow.domain.rules;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationResultTest {

    @Test
    void ok_shouldReturnValidResult() {
        var result = ValidationResult.ok();
        assertThat(result.isValid()).isTrue();
        assertThat(result.errorMessage()).isNull();
    }

    @Test
    void failure_shouldReturnInvalidResult() {
        var result = ValidationResult.failure("Error message");
        assertThat(result.isValid()).isFalse();
        assertThat(result.errorMessage()).isEqualTo("Error message");
    }
}
