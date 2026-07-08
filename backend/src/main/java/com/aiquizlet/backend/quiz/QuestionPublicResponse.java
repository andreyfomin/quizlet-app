package com.aiquizlet.backend.quiz;

import java.util.List;

public record QuestionPublicResponse(Long id, String text, List<String> options) {

    public static QuestionPublicResponse from(Question question) {
        return new QuestionPublicResponse(question.getId(), question.getText(), question.getOptions());
    }
}
