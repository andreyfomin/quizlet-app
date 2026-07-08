package com.aiquizlet.slackbot.backend;

import java.util.List;

record StartSessionRequest(Long quizletId, List<String> slackUserIds) {
}
