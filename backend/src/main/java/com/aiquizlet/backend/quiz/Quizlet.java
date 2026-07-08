package com.aiquizlet.backend.quiz;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Quizlet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String topic;

    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "quizlet", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<Question> questions = new ArrayList<>();

    protected Quizlet() {
    }

    public Quizlet(String topic) {
        this.topic = topic;
    }

    public Long getId() {
        return id;
    }

    public String getTopic() {
        return topic;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void addQuestion(Question question) {
        question.setQuizlet(this);
        question.setOrderIndex(questions.size());
        questions.add(question);
    }
}
