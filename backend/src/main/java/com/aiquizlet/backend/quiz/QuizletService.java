package com.aiquizlet.backend.quiz;

import com.aiquizlet.backend.ai.QuizGeneratorService;
import com.aiquizlet.backend.common.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class QuizletService {

    private final QuizletRepository quizletRepository;
    private final QuizGeneratorService quizGeneratorService;

    public QuizletService(QuizletRepository quizletRepository, QuizGeneratorService quizGeneratorService) {
        this.quizletRepository = quizletRepository;
        this.quizGeneratorService = quizGeneratorService;
    }

    @Transactional
    public Quizlet createQuizlet(String topic, int questionCount) {
        var generated = quizGeneratorService.generate(topic, questionCount);

        Quizlet quizlet = new Quizlet(topic);
        generated.questions().forEach(q ->
                quizlet.addQuestion(new Question(q.text(), q.options(), q.correctOptionIndex())));

        return quizletRepository.save(quizlet);
    }

    @Transactional(readOnly = true)
    public Quizlet getQuizlet(Long id) {
        return quizletRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Quizlet " + id + " not found"));
    }

    @Transactional(readOnly = true)
    public List<Quizlet> listQuizlets() {
        return quizletRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public void deleteQuizlet(Long id) {
        Quizlet quizlet = getQuizlet(id);
        // Deleting a quizlet that still has sessions referencing it violates the
        // quiz_session.quizlet_id FK constraint; ApiExceptionHandler turns that into
        // a 409 rather than a raw 500.
        quizletRepository.delete(quizlet);
    }
}
