package com.aiquizlet.slackbot.backend;

record SubmitAnswerRequest(String slackUserId, int selectedOptionIndex) {
}
