package com.aiquizlet.slackbot.backend;

import java.util.List;

public record QuizletResponse(Long id, String topic, List<QuestionResponse> questions) {
}
