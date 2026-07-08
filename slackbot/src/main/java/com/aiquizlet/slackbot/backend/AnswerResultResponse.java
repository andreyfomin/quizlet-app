package com.aiquizlet.slackbot.backend;

public record AnswerResultResponse(boolean correct, int correctOptionIndex, int score, boolean sessionCompleted,
                                    QuestionPublicResponse nextQuestion) {
}
