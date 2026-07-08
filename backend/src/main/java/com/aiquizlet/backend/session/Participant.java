package com.aiquizlet.backend.session;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"session_id", "slack_user_id"}))
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "session_id")
    private QuizSession session;

    @Column(name = "slack_user_id")
    private String slackUserId;

    private int score;

    private int currentQuestionIndex;

    private boolean completed;

    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("answeredAt ASC")
    private List<Answer> answers = new ArrayList<>();

    protected Participant() {
    }

    public Participant(String slackUserId) {
        this.slackUserId = slackUserId;
    }

    public Long getId() {
        return id;
    }

    public QuizSession getSession() {
        return session;
    }

    void setSession(QuizSession session) {
        this.session = session;
    }

    public String getSlackUserId() {
        return slackUserId;
    }

    public int getScore() {
        return score;
    }

    public int getCurrentQuestionIndex() {
        return currentQuestionIndex;
    }

    public boolean isCompleted() {
        return completed;
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public void recordAnswer(Answer answer, boolean correct, int totalQuestions) {
        answer.setParticipant(this);
        answers.add(answer);
        if (correct) {
            score++;
        }
        currentQuestionIndex++;
        if (currentQuestionIndex >= totalQuestions) {
            completed = true;
        }
    }
}
