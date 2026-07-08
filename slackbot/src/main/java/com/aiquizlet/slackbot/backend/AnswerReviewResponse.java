package com.aiquizlet.slackbot.backend;

import java.util.List;

public record AnswerReviewResponse(String questionText, List<String> options, int selectedOptionIndex,
                                    int correctOptionIndex, boolean correct) {
}
