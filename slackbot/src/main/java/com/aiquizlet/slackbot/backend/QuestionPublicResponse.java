package com.aiquizlet.slackbot.backend;

import java.util.List;

public record QuestionPublicResponse(Long id, String text, List<String> options) {
}
