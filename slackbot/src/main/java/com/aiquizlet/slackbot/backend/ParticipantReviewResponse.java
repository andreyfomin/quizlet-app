package com.aiquizlet.slackbot.backend;

import java.util.List;

public record ParticipantReviewResponse(String slackUserId, int score, int totalQuestions,
                                         List<AnswerReviewResponse> answers) {
}
