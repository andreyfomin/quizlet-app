package com.aiquizlet.backend.session;

import com.aiquizlet.backend.common.NotFoundException;
import com.aiquizlet.backend.quiz.Question;
import com.aiquizlet.backend.quiz.Quizlet;
import com.aiquizlet.backend.quiz.QuizletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class QuizSessionService {

    private final QuizSessionRepository sessionRepository;
    private final QuizletRepository quizletRepository;
    private final ParticipantRepository participantRepository;

    public QuizSessionService(QuizSessionRepository sessionRepository,
                               QuizletRepository quizletRepository,
                               ParticipantRepository participantRepository) {
        this.sessionRepository = sessionRepository;
        this.quizletRepository = quizletRepository;
        this.participantRepository = participantRepository;
    }

    @Transactional
    public QuizSession startSession(Long quizletId, List<String> slackUserIds) {
        Quizlet quizlet = quizletRepository.findById(quizletId)
                .orElseThrow(() -> new NotFoundException("Quizlet " + quizletId + " not found"));
        if (quizlet.getQuestions().isEmpty()) {
            throw new IllegalStateException("Quizlet " + quizletId + " has no questions");
        }
        if (slackUserIds == null || slackUserIds.isEmpty()) {
            throw new IllegalArgumentException("At least one Slack user id is required to start a session");
        }

        QuizSession session = new QuizSession(quizlet);
        slackUserIds.forEach(userId -> session.addParticipant(new Participant(userId)));

        return sessionRepository.save(session);
    }

    @Transactional(readOnly = true)
    public QuizSession getSession(Long sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Session " + sessionId + " not found"));
    }

    @Transactional
    public AnswerOutcome submitAnswer(Long sessionId, String slackUserId, int selectedOptionIndex) {
        QuizSession session = getSession(sessionId);
        Participant participant = participantRepository.findBySession_IdAndSlackUserId(sessionId, slackUserId)
                .orElseThrow(() -> new NotFoundException(
                        "Participant " + slackUserId + " is not part of session " + sessionId));

        if (participant.isCompleted()) {
            throw new IllegalStateException("Participant " + slackUserId + " has already completed the quiz");
        }

        List<Question> questions = session.getQuizlet().getQuestions();
        Question question = questions.get(participant.getCurrentQuestionIndex());

        if (selectedOptionIndex < 0 || selectedOptionIndex >= question.getOptions().size()) {
            throw new IllegalArgumentException("selectedOptionIndex " + selectedOptionIndex
                    + " is out of range for question " + question.getId()
                    + " (" + question.getOptions().size() + " options)");
        }

        boolean correct = question.getCorrectOptionIndex() == selectedOptionIndex;

        Answer answer = new Answer(question, selectedOptionIndex, correct);
        participant.recordAnswer(answer, correct, questions.size());

        if (session.getParticipants().stream().allMatch(Participant::isCompleted)) {
            session.setStatus(SessionStatus.COMPLETED);
        }

        Question nextQuestion = participant.isCompleted() ? null : questions.get(participant.getCurrentQuestionIndex());
        return new AnswerOutcome(correct, question.getCorrectOptionIndex(), participant.getScore(),
                participant.isCompleted(), nextQuestion);
    }

    record AnswerOutcome(boolean correct, int correctOptionIndex, int score, boolean participantCompleted,
                          Question nextQuestion) {
    }
}
