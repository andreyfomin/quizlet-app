package com.aiquizlet.backend.quiz;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/quizlets")
public class QuizletController {

    private final QuizletService quizletService;

    public QuizletController(QuizletService quizletService) {
        this.quizletService = quizletService;
    }

    @PostMapping
    public ResponseEntity<QuizletResponse> createQuizlet(@RequestBody CreateQuizletRequest request) {
        Quizlet quizlet = quizletService.createQuizlet(request.topic(), request.questionCountOrDefault());
        return ResponseEntity.status(HttpStatus.CREATED).body(QuizletResponse.from(quizlet));
    }

    @GetMapping
    public List<QuizletSummaryResponse> listQuizlets() {
        return quizletService.listQuizlets().stream().map(QuizletSummaryResponse::from).toList();
    }

    @GetMapping("/{id}")
    public QuizletResponse getQuizlet(@PathVariable Long id) {
        return QuizletResponse.from(quizletService.getQuizlet(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuizlet(@PathVariable Long id) {
        quizletService.deleteQuizlet(id);
        return ResponseEntity.noContent().build();
    }
}
