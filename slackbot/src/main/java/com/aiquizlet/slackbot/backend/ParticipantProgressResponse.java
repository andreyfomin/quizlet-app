package com.aiquizlet.slackbot.backend;

public record ParticipantProgressResponse(String slackUserId, int score, int questionsAnswered, int totalQuestions,
                                           boolean completed) {
}
