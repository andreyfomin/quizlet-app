package com.aiquizlet.backend.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class HuggingFaceConfig {

    @Bean
    public RestClient huggingFaceRestClient(
            @Value("${huggingface.api-base-url:https://router.huggingface.co/v1}") String baseUrl,
            @Value("${HF_API_TOKEN:}") String apiToken) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiToken)
                .build();
    }
}
