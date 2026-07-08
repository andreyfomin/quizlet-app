package com.aiquizlet.backend.session;

import com.aiquizlet.backend.common.NotFoundException;
import com.aiquizlet.backend.quiz.QuestionPublicResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
public class QuizSessionController {

    private final QuizSessionService sessionService;

    public QuizSessionController(QuizSessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    public ResponseEntity<SessionResponse> startSession(@RequestBody StartSessionRequest request) {
        QuizSession session = sessionService.startSession(request.quizletId(), request.slackUserIds());
        return ResponseEntity.status(HttpStatus.CREATED).body(toSessionResponse(session));
    }

    @PostMapping("/{sessionId}/answers")
    public AnswerResultResponse submitAnswer(@PathVariable Long sessionId, @RequestBody SubmitAnswerRequest request) {
        var outcome = sessionService.submitAnswer(sessionId, request.slackUserId(), request.selectedOptionIndex());
        QuestionPublicResponse nextQuestion = outcome.nextQuestion() == null
                ? null
                : QuestionPublicResponse.from(outcome.nextQuestion());
        return new AnswerResultResponse(outcome.correct(), outcome.correctOptionIndex(), outcome.score(),
                outcome.participantCompleted(), nextQuestion);
    }

    @GetMapping("/{sessionId}/progress")
    public List<ParticipantProgressResponse> getProgress(@PathVariable Long sessionId) {
        QuizSession session = sessionService.getSession(sessionId);
        int totalQuestions = session.getQuizlet().getQuestions().size();
        return session.getParticipants().stream()
                .map(p -> ParticipantProgressResponse.from(p, totalQuestions))
                .toList();
    }

    @GetMapping("/{sessionId}/progress/{slackUserId}")
    public ParticipantProgressResponse getUserProgress(@PathVariable Long sessionId, @PathVariable String slackUserId) {
        QuizSession session = sessionService.getSession(sessionId);
        int totalQuestions = session.getQuizlet().getQuestions().size();
        return session.getParticipants().stream()
                .filter(p -> p.getSlackUserId().equals(slackUserId))
                .findFirst()
                .map(p -> ParticipantProgressResponse.from(p, totalQuestions))
                .orElseThrow(() -> new NotFoundException(
                        "Participant " + slackUserId + " not found in session " + sessionId));
    }

    @GetMapping("/{sessionId}/review/{slackUserId}")
    public ParticipantReviewResponse getUserReview(@PathVariable Long sessionId, @PathVariable String slackUserId) {
        QuizSession session = sessionService.getSession(sessionId);
        int totalQuestions = session.getQuizlet().getQuestions().size();
        return session.getParticipants().stream()
                .filter(p -> p.getSlackUserId().equals(slackUserId))
                .findFirst()
                .map(p -> ParticipantReviewResponse.from(p, totalQuestions))
                .orElseThrow(() -> new NotFoundException(
                        "Participant " + slackUserId + " not found in session " + sessionId));
    }

    private SessionResponse toSessionResponse(QuizSession session) {
        var participants = session.getParticipants().stream().map(ParticipantResponse::from).toList();
        var questions = session.getQuizlet().getQuestions();
        QuestionPublicResponse currentQuestion = questions.isEmpty() ? null : QuestionPublicResponse.from(questions.get(0));
        return new SessionResponse(session.getId(), session.getQuizlet().getId(), session.getStatus(), participants,
                currentQuestion);
    }
}
