package com.aiquizlet.backend.quiz;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "quizlet_id")
    private Quizlet quizlet;

    private int orderIndex;

    @Column(length = 1000)
    private String text;

    @ElementCollection
    @CollectionTable(name = "question_options", joinColumns = @JoinColumn(name = "question_id"))
    @OrderColumn(name = "option_index")
    @Column(name = "option_text", length = 500)
    private List<String> options = new ArrayList<>();

    private int correctOptionIndex;

    protected Question() {
    }

    public Question(String text, List<String> options, int correctOptionIndex) {
        this.text = text;
        this.options = new ArrayList<>(options);
        this.correctOptionIndex = correctOptionIndex;
    }

    public Long getId() {
        return id;
    }

    public Quizlet getQuizlet() {
        return quizlet;
    }

    void setQuizlet(Quizlet quizlet) {
        this.quizlet = quizlet;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    public String getText() {
        return text;
    }

    public List<String> getOptions() {
        return options;
    }

    public int getCorrectOptionIndex() {
        return correctOptionIndex;
    }
}
