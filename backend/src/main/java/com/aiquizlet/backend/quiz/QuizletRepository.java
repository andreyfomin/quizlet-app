package com.aiquizlet.backend.quiz;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizletRepository extends JpaRepository<Quizlet, Long> {

    List<Quizlet> findAllByOrderByCreatedAtDesc();
}
