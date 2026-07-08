package com.aiquizlet.slackbot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Backs the slash-command handlers that must ack Slack within 3 seconds but call a
 * potentially slow backend endpoint (quiz generation via the AI provider can take
 * several seconds) — those handlers ack immediately and finish the real work here.
 */
@Configuration
public class AsyncConfig {

    @Bean(destroyMethod = "shutdown")
    public ExecutorService slackTaskExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
