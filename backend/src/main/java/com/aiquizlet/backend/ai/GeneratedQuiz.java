package com.aiquizlet.backend.ai;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record GeneratedQuiz(
        @JsonPropertyDescription("The generated quiz questions")
        List<GeneratedQuestion> questions) {
}
