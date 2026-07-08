package com.aiquizlet.slackbot.backend;

public record ParticipantResponse(String slackUserId, int score, int currentQuestionIndex, boolean completed) {
}
