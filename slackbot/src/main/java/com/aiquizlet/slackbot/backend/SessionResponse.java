package com.aiquizlet.slackbot.backend;

import java.util.List;

public record SessionResponse(Long id, Long quizletId, String status, List<ParticipantResponse> participants,
                               QuestionPublicResponse currentQuestion) {
}
