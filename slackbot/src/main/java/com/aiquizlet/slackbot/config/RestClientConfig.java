package com.aiquizlet.slackbot.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    @Qualifier("backendRestClient")
    public RestClient backendRestClient(@Value("${backend.base-url}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).build();
    }

    /**
     * No base URL: used to POST to whatever Slack response_url a command/action carries,
     * which is a different host per interaction.
     */
    @Bean
    @Qualifier("slackWebhookRestClient")
    public RestClient slackWebhookRestClient() {
        return RestClient.builder().build();
    }
}
