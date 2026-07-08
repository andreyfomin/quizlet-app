package com.aiquizlet.slackbot.quiz;

import com.aiquizlet.slackbot.backend.QuestionPublicResponse;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.methods.response.chat.ChatUpdateResponse;
import com.slack.api.methods.response.conversations.ConversationsOpenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * Delivers quiz questions and results to individual Slack users via DM. Opening a DM
 * and posting/updating messages both go through the Slack Web API (bot token), unlike
 * the slash-command replies in {@link SlackResponder} which use response_url webhooks.
 *
 * <p>The Java Slack SDK does <b>not</b> throw on an API-level failure (e.g.
 * {@code missing_scope}, {@code channel_not_found}) — it returns {@code ok: false} on
 * the response object like any other field, and only throws {@link SlackApiException}
 * for transport-level problems. Every call here is followed by an explicit
 * {@code isOk()} check for that reason; skipping it would silently treat a failed send
 * as a success.
 */
@Component
public class QuizMessenger {

    private static final Logger log = LoggerFactory.getLogger(QuizMessenger.class);

    /** Sends a brand-new question to a participant in a fresh DM. */
    public void sendQuestion(MethodsClient client, String slackUserId, Long sessionId,
                              QuestionPublicResponse question) throws IOException, SlackApiException {
        String channelId = openDirectMessage(client, slackUserId);
        ChatPostMessageResponse response = client.chatPostMessage(r -> r
                .channel(channelId)
                .text(question.text())
                .blocks(SlackBlocks.question(sessionId, question)));
        if (!response.isOk()) {
            throw new IllegalStateException("chat.postMessage failed: " + response.getError());
        }
        log.info("Sent question for session {} to user {} in DM channel {} (message ts {})",
                sessionId, slackUserId, channelId, response.getTs());
    }

    /** Replaces the just-answered question's message with feedback + the next question. */
    public void updateWithNextQuestion(MethodsClient client, String channelId, String messageTs, Long sessionId,
                                        String headerText, QuestionPublicResponse nextQuestion)
            throws IOException, SlackApiException {
        ChatUpdateResponse response = client.chatUpdate(r -> r
                .channel(channelId)
                .ts(messageTs)
                .text(headerText)
                .blocks(SlackBlocks.question(sessionId, nextQuestion, headerText)));
        if (!response.isOk()) {
            throw new IllegalStateException("chat.update failed: " + response.getError());
        }
    }

    /** Replaces the just-answered question's message with the final result (no more questions). */
    public void updateWithFinalResult(MethodsClient client, String channelId, String messageTs, String text)
            throws IOException, SlackApiException {
        ChatUpdateResponse response = client.chatUpdate(r -> r
                .channel(channelId)
                .ts(messageTs)
                .text(text)
                .blocks(SlackBlocks.finalResult(text)));
        if (!response.isOk()) {
            throw new IllegalStateException("chat.update failed: " + response.getError());
        }
    }

    private String openDirectMessage(MethodsClient client, String slackUserId) throws IOException, SlackApiException {
        ConversationsOpenResponse response = client.conversationsOpen(r -> r.users(List.of(slackUserId)));
        if (!response.isOk()) {
            throw new IllegalStateException("conversations.open failed: " + response.getError());
        }
        String channelId = response.getChannel().getId();
        log.info("Opened DM channel {} with user {}", channelId, slackUserId);
        return channelId;
    }
}
