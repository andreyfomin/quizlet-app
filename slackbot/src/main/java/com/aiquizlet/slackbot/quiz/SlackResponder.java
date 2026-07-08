package com.aiquizlet.slackbot.quiz;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Posts a delayed reply to a Slack {@code response_url}. Slash commands and block
 * actions each carry their own response_url; it accepts a plain webhook-style POST
 * for up to ~30 minutes / 5 uses after the original interaction, which is how we
 * deliver results that take longer to produce than Slack's 3-second ack window.
 */
@Component
public class SlackResponder {

    private final RestClient restClient;

    public SlackResponder(@Qualifier("slackWebhookRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public void respond(String responseUrl, String text) {
        restClient.post()
                .uri(responseUrl)
                .body(Map.of("response_type", "ephemeral", "text", text))
                .retrieve()
                .toBodilessEntity();
    }
}
