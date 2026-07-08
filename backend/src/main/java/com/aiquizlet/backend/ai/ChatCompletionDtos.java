package com.aiquizlet.backend.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

record ChatMessage(String role, String content) {
}

@JsonInclude(JsonInclude.Include.NON_NULL)
record ChatCompletionRequest(
        String model,
        List<ChatMessage> messages,
        Double temperature,
        @JsonProperty("max_tokens") Integer maxTokens) {
}

@JsonIgnoreProperties(ignoreUnknown = true)
record ChatCompletionResponse(List<Choice> choices) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Choice(ChatMessage message) {
    }
}
