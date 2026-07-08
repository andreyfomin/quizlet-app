package com.aiquizlet.backend.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class QuizGeneratorService {

    private final RestClient huggingFaceRestClient;
    private final ObjectMapper objectMapper;
    private final String model;

    public QuizGeneratorService(RestClient huggingFaceRestClient, ObjectMapper objectMapper,
                                 @Value("${huggingface.model:meta-llama/Llama-3.1-8B-Instruct}") String model) {
        this.huggingFaceRestClient = huggingFaceRestClient;
        this.objectMapper = objectMapper;
        this.model = model;
    }

    public GeneratedQuiz generate(String topic, int questionCount) {
        String prompt = """
                Generate %d multiple-choice quiz questions about "%s".

                Respond with ONLY a single JSON object (no markdown, no commentary, no code fences) matching \
                exactly this shape:
                {"questions": [{"text": "...", "options": ["...", "...", "...", "..."], "correctOptionIndex": 0}]}

                Each question must have exactly 4 options in the "options" array, and "correctOptionIndex" must \
                be a zero-based index into that array pointing at the correct answer.
                """.formatted(questionCount, topic);

        ChatCompletionRequest request = new ChatCompletionRequest(
                model,
                List.of(new ChatMessage("user", prompt)),
                0.7,
                2048);

        ChatCompletionResponse response = huggingFaceRestClient.post()
                .uri("/chat/completions")
                .body(request)
                .retrieve()
                .body(ChatCompletionResponse.class);

        String content = response.choices().stream()
                .findFirst()
                .map(choice -> choice.message().content())
                .orElseThrow(() -> new IllegalStateException("Hugging Face returned no completion for topic: " + topic));

        return parseQuiz(content, topic);
    }

    private GeneratedQuiz parseQuiz(String content, String topic) {
        String json = extractJsonObject(content, topic);
        try {
            return objectMapper.readValue(json, GeneratedQuiz.class);
        } catch (Exception e) {
            throw new IllegalStateException("Could not parse quiz JSON from Hugging Face for topic: " + topic, e);
        }
    }

    private String extractJsonObject(String content, String topic) {
        int start = content.indexOf('{');
        int end = content.lastIndexOf('}');
        if (start < 0 || end < start) {
            throw new IllegalStateException(
                    "Hugging Face response for topic \"" + topic + "\" did not contain a JSON object: " + content);
        }
        return content.substring(start, end + 1);
    }
}
