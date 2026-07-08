package com.aiquizlet.backend.session;

import com.aiquizlet.backend.quiz.Question;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.Instant;

@Entity
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "participant_id")
    private Participant participant;

    @ManyToOne(optional = false)
    @JoinColumn(name = "question_id")
    private Question question;

    private int selectedOptionIndex;

    private boolean correct;

    private Instant answeredAt = Instant.now();

    protected Answer() {
    }

    public Answer(Question question, int selectedOptionIndex, boolean correct) {
        this.question = question;
        this.selectedOptionIndex = selectedOptionIndex;
        this.correct = correct;
    }

    public Long getId() {
        return id;
    }

    public Participant getParticipant() {
        return participant;
    }

    void setParticipant(Participant participant) {
        this.participant = participant;
    }

    public Question getQuestion() {
        return question;
    }

    public int getSelectedOptionIndex() {
        return selectedOptionIndex;
    }

    public boolean isCorrect() {
        return correct;
    }

    public Instant getAnsweredAt() {
        return answeredAt;
    }
}
