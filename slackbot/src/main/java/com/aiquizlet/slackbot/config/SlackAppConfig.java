package com.aiquizlet.slackbot.config;

import com.aiquizlet.slackbot.quiz.AnswerActionService;
import com.aiquizlet.slackbot.quiz.QuizCommandService;
import com.aiquizlet.slackbot.quiz.SlackBlocks;
import com.slack.api.bolt.App;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.regex.Pattern;

/**
 * Wires up the Bolt {@link App}: reads SLACK_BOT_TOKEN / SLACK_SIGNING_SECRET from the
 * environment automatically, then registers the /quiz slash command and the quiz
 * answer button handler. Requests actually reach these via {@link SlackAppServletController}.
 */
@Configuration
public class SlackAppConfig {

    @Bean
    public App slackApp(QuizCommandService quizCommandService, AnswerActionService answerActionService) {
        App app = new App();
        app.command("/quiz", quizCommandService::handle);
        app.blockAction(Pattern.compile(SlackBlocks.ANSWER_ACTION_ID_PATTERN), answerActionService::handle);
        return app;
    }
}
