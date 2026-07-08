package com.aiquizlet.backend.session;

import com.aiquizlet.backend.quiz.Question;
import com.aiquizlet.backend.quiz.QuestionPublicResponse;

import java.util.List;

record StartSessionRequest(Long quizletId, List<String> slackUserIds) {
}

record ParticipantResponse(String slackUserId, int score, int currentQuestionIndex, boolean completed) {

    static ParticipantResponse from(Participant participant) {
        return new ParticipantResponse(
                participant.getSlackUserId(),
                participant.getScore(),
                participant.getCurrentQuestionIndex(),
                participant.isCompleted());
    }
}

record SessionResponse(Long id, Long quizletId, SessionStatus status, List<ParticipantResponse> participants,
                        QuestionPublicResponse currentQuestion) {
}

record SubmitAnswerRequest(String slackUserId, int selectedOptionIndex) {
}

record AnswerResultResponse(boolean correct, int correctOptionIndex, int score, boolean sessionCompleted,
                             QuestionPublicResponse nextQuestion) {
}

record ParticipantProgressResponse(String slackUserId, int score, int questionsAnswered, int totalQuestions,
                                    boolean completed) {

    static ParticipantProgressResponse from(Participant participant, int totalQuestions) {
        return new ParticipantProgressResponse(
                participant.getSlackUserId(),
                participant.getScore(),
                participant.getCurrentQuestionIndex(),
                totalQuestions,
                participant.isCompleted());
    }
}

record AnswerReviewResponse(String questionText, List<String> options, int selectedOptionIndex,
                             int correctOptionIndex, boolean correct) {

    static AnswerReviewResponse from(Answer answer) {
        Question question = answer.getQuestion();
        return new AnswerReviewResponse(
                question.getText(),
                question.getOptions(),
                answer.getSelectedOptionIndex(),
                question.getCorrectOptionIndex(),
                answer.isCorrect());
    }
}

record ParticipantReviewResponse(String slackUserId, int score, int totalQuestions,
                                  List<AnswerReviewResponse> answers) {

    static ParticipantReviewResponse from(Participant participant, int totalQuestions) {
        return new ParticipantReviewResponse(
                participant.getSlackUserId(),
                participant.getScore(),
                totalQuestions,
                participant.getAnswers().stream().map(AnswerReviewResponse::from).toList());
    }
}
