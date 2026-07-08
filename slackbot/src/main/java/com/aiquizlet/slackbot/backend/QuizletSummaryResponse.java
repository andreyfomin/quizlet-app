package com.aiquizlet.slackbot.backend;

import java.time.Instant;

public record QuizletSummaryResponse(Long id, String topic, int questionCount, Instant createdAt) {
}
