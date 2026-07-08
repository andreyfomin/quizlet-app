package com.aiquizlet.backend.ai;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record GeneratedQuestion(
        @JsonPropertyDescription("The question text")
        String text,

        @JsonPropertyDescription("Exactly four possible answers")
        List<String> options,

        @JsonPropertyDescription("Zero-based index into options pointing at the correct answer")
        int correctOptionIndex) {
}
