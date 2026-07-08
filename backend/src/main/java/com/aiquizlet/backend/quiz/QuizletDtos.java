package com.aiquizlet.backend.quiz;

import java.time.Instant;
import java.util.List;

record CreateQuizletRequest(String topic, Integer questionCount) {

    private static final int DEFAULT_QUESTION_COUNT = 5;

    int questionCountOrDefault() {
        return questionCount == null ? DEFAULT_QUESTION_COUNT : questionCount;
    }
}

record QuestionResponse(Long id, String text, List<String> options, int correctOptionIndex) {

    static QuestionResponse from(Question question) {
        return new QuestionResponse(question.getId(), question.getText(), question.getOptions(),
                question.getCorrectOptionIndex());
    }
}

record QuizletResponse(Long id, String topic, Instant createdAt, List<QuestionResponse> questions) {

    static QuizletResponse from(Quizlet quizlet) {
        return new QuizletResponse(
                quizlet.getId(),
                quizlet.getTopic(),
                quizlet.getCreatedAt(),
                quizlet.getQuestions().stream().map(QuestionResponse::from).toList());
    }
}

record QuizletSummaryResponse(Long id, String topic, int questionCount, Instant createdAt) {

    static QuizletSummaryResponse from(Quizlet quizlet) {
        return new QuizletSummaryResponse(
                quizlet.getId(),
                quizlet.getTopic(),
                quizlet.getQuestions().size(),
                quizlet.getCreatedAt());
    }
}
