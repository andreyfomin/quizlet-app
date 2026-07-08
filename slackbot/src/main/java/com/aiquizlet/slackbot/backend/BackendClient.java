package com.aiquizlet.slackbot.backend;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class BackendClient {

    private final RestClient restClient;

    public BackendClient(@Qualifier("backendRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public QuizletResponse createQuizlet(String topic, Integer questionCount) {
        return restClient.post()
                .uri("/api/quizlets")
                .body(new CreateQuizletRequest(topic, questionCount))
                .retrieve()
                .body(QuizletResponse.class);
    }

    public List<QuizletSummaryResponse> listQuizlets() {
        return restClient.get()
                .uri("/api/quizlets")
                .retrieve()
                .body(new ParameterizedTypeReference<List<QuizletSummaryResponse>>() {
                });
    }

    public void deleteQuizlet(Long quizletId) {
        restClient.delete()
                .uri("/api/quizlets/{id}", quizletId)
                .retrieve()
                .toBodilessEntity();
    }

    public SessionResponse startSession(Long quizletId, List<String> slackUserIds) {
        return restClient.post()
                .uri("/api/sessions")
                .body(new StartSessionRequest(quizletId, slackUserIds))
                .retrieve()
                .body(SessionResponse.class);
    }

    public AnswerResultResponse submitAnswer(Long sessionId, String slackUserId, int selectedOptionIndex) {
        return restClient.post()
                .uri("/api/sessions/{sessionId}/answers", sessionId)
                .body(new SubmitAnswerRequest(slackUserId, selectedOptionIndex))
                .retrieve()
                .body(AnswerResultResponse.class);
    }

    public List<ParticipantProgressResponse> getProgress(Long sessionId) {
        return restClient.get()
                .uri("/api/sessions/{sessionId}/progress", sessionId)
                .retrieve()
                .body(new ParameterizedTypeReference<List<ParticipantProgressResponse>>() {
                });
    }

    public ParticipantReviewResponse getReview(Long sessionId, String slackUserId) {
        return restClient.get()
                .uri("/api/sessions/{sessionId}/review/{slackUserId}", sessionId, slackUserId)
                .retrieve()
                .body(ParticipantReviewResponse.class);
    }
}
