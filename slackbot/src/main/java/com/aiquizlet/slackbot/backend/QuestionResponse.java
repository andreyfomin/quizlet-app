package com.aiquizlet.slackbot.backend;

import java.util.List;

public record QuestionResponse(Long id, String text, List<String> options, Integer correctOptionIndex) {
}
