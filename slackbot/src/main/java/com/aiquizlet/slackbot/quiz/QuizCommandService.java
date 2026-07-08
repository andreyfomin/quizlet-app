package com.aiquizlet.slackbot.quiz;

import com.aiquizlet.slackbot.backend.BackendClient;
import com.aiquizlet.slackbot.backend.ParticipantProgressResponse;
import com.aiquizlet.slackbot.backend.ParticipantResponse;
import com.aiquizlet.slackbot.backend.QuizletResponse;
import com.aiquizlet.slackbot.backend.QuizletSummaryResponse;
import com.aiquizlet.slackbot.backend.SessionResponse;
import com.slack.api.bolt.context.builtin.SlashCommandContext;
import com.slack.api.bolt.request.builtin.SlashCommandRequest;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.MethodsClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles the {@code /quiz} slash command: {@code create}, {@code list}, {@code delete},
 * {@code start}, and {@code progress} subcommands.
 *
 * <p>{@code create} and {@code start} call the backend (the former triggers an AI
 * generation call that can take several seconds), which would blow Slack's 3-second
 * ack window if done inline. Both ack immediately with a placeholder message, do the
 * real work on {@link #executor}, and deliver the result via {@link SlackResponder}
 * once it's ready. {@code list}, {@code delete}, and {@code progress} are single fast
 * backend calls, so they reply synchronously.
 */
@Service
public class QuizCommandService {

    private static final Pattern USER_MENTION = Pattern.compile("<@([A-Z0-9]+)(?:\\|[^>]*)?>");

    private final BackendClient backendClient;
    private final QuizMessenger messenger;
    private final SlackResponder responder;
    private final ExecutorService executor;

    public QuizCommandService(BackendClient backendClient, QuizMessenger messenger, SlackResponder responder,
                               ExecutorService slackTaskExecutor) {
        this.backendClient = backendClient;
        this.messenger = messenger;
        this.responder = responder;
        this.executor = slackTaskExecutor;
    }

    public Response handle(SlashCommandRequest req, SlashCommandContext ctx) {
        String text = req.getPayload().getText() == null ? "" : req.getPayload().getText().trim();
        String responseUrl = req.getPayload().getResponseUrl();

        if (text.equals("create") || text.startsWith("create ")) {
            return handleCreate(afterFirstWord(text), responseUrl, ctx);
        }
        if (text.equals("start") || text.startsWith("start ")) {
            return handleStart(afterFirstWord(text), responseUrl, ctx);
        }
        if (text.equals("progress") || text.startsWith("progress ")) {
            return handleProgress(afterFirstWord(text), ctx);
        }
        if (text.equals("list")) {
            return handleList(ctx);
        }
        if (text.equals("delete") || text.startsWith("delete ")) {
            return handleDelete(afterFirstWord(text), ctx);
        }
        if (text.isBlank() || text.equals("help")) {
            return ctx.ack(usage());
        }
        return ctx.ack(":grey_question: Unknown command `" + text + "`.\n\n" + usage());
    }

    private Response handleCreate(String rest, String responseUrl, SlashCommandContext ctx) {
        if (rest.isBlank()) {
            return ctx.ack("Usage: `/quiz create <topic>` (optionally `/quiz create <topic> | <questionCount>`)");
        }

        String topic = rest;
        Integer questionCount = null;
        int sep = rest.lastIndexOf('|');
        if (sep >= 0) {
            try {
                questionCount = Integer.parseInt(rest.substring(sep + 1).trim());
                topic = rest.substring(0, sep).trim();
            } catch (NumberFormatException ignored) {
                // trailing text after "|" wasn't a number — treat the whole thing as the topic
            }
        }

        String finalTopic = topic;
        Integer finalCount = questionCount;
        executor.submit(() -> {
            try {
                QuizletResponse quizlet = backendClient.createQuizlet(finalTopic, finalCount);
                responder.respond(responseUrl, ":white_check_mark: Created quizlet #" + quizlet.id()
                        + " on *" + quizlet.topic() + "* (" + quizlet.questions().size() + " questions).\n"
                        + "Start it with `/quiz start " + quizlet.id() + " @user1 @user2 ...`");
            } catch (Exception e) {
                responder.respond(responseUrl, ":x: Failed to create quiz: " + e.getMessage());
            }
        });
        return ctx.ack(":hourglass_flowing_sand: Generating your quiz on *" + topic + "*, this can take a few seconds...");
    }

    private Response handleStart(String rest, String responseUrl, SlashCommandContext ctx) {
        String[] parts = rest.split("\\s+", 2);
        if (parts.length < 2) {
            return ctx.ack("Usage: `/quiz start <quizletId> @user1 @user2 ...`");
        }

        long quizletId;
        try {
            quizletId = Long.parseLong(parts[0]);
        } catch (NumberFormatException e) {
            return ctx.ack(":x: `" + parts[0] + "` is not a valid quizlet id.");
        }

        List<String> userIds = new ArrayList<>();
        Matcher matcher = USER_MENTION.matcher(parts[1]);
        while (matcher.find()) {
            userIds.add(matcher.group(1));
        }
        if (userIds.isEmpty()) {
            return ctx.ack("Mention at least one user to start the quiz with, e.g. `/quiz start 12 @alice @bob`");
        }

        MethodsClient client = ctx.client();
        executor.submit(() -> {
            try {
                SessionResponse session = backendClient.startSession(quizletId, userIds);
                deliverFirstQuestions(client, session);
                responder.respond(responseUrl, ":rocket: Started session #" + session.id() + " with "
                        + session.participants().size() + " participant(s). Everyone's been sent question 1 by DM.");
            } catch (Exception e) {
                responder.respond(responseUrl, ":x: Failed to start session: " + e.getMessage());
            }
        });
        return ctx.ack(":hourglass_flowing_sand: Starting quiz session...");
    }

    private Response handleProgress(String rest, SlashCommandContext ctx) {
        if (rest.isBlank()) {
            return ctx.ack("Usage: `/quiz progress <sessionId>`");
        }
        long sessionId;
        try {
            sessionId = Long.parseLong(rest);
        } catch (NumberFormatException e) {
            return ctx.ack(":x: `" + rest + "` is not a valid session id.");
        }

        List<ParticipantProgressResponse> progress = backendClient.getProgress(sessionId);
        StringBuilder sb = new StringBuilder(":bar_chart: *Progress for session #" + sessionId + "*\n");
        for (ParticipantProgressResponse p : progress) {
            sb.append("• <@").append(p.slackUserId()).append("> — ")
                    .append(p.score()).append("/").append(p.totalQuestions());
            if (p.completed()) {
                sb.append(" :checkered_flag:");
            } else {
                sb.append(" (").append(p.questionsAnswered()).append("/").append(p.totalQuestions()).append(" answered)");
            }
            sb.append("\n");
        }
        return ctx.ack(sb.toString());
    }

    private Response handleList(SlashCommandContext ctx) {
        List<QuizletSummaryResponse> quizlets = backendClient.listQuizlets();
        if (quizlets.isEmpty()) {
            return ctx.ack("No quizzes yet — create one with `/quiz create <topic>`.");
        }

        StringBuilder sb = new StringBuilder(":books: *Available quizzes*\n");
        for (QuizletSummaryResponse q : quizlets) {
            sb.append("• #").append(q.id()).append(" — ").append(q.topic())
                    .append(" (").append(q.questionCount()).append(" questions)\n");
        }
        sb.append("Start one with `/quiz start <id> @user1 @user2 ...`");
        return ctx.ack(sb.toString());
    }

    private Response handleDelete(String rest, SlashCommandContext ctx) {
        if (rest.isBlank()) {
            return ctx.ack("Usage: `/quiz delete <quizletId>`");
        }
        long quizletId;
        try {
            quizletId = Long.parseLong(rest);
        } catch (NumberFormatException e) {
            return ctx.ack(":x: `" + rest + "` is not a valid quizlet id.");
        }

        try {
            backendClient.deleteQuizlet(quizletId);
            return ctx.ack(":wastebasket: Deleted quizlet #" + quizletId + ".");
        } catch (HttpClientErrorException.NotFound e) {
            return ctx.ack(":x: Quizlet " + quizletId + " not found.");
        } catch (HttpClientErrorException.Conflict e) {
            return ctx.ack(":x: Can't delete quizlet " + quizletId
                    + " — it has one or more sessions that reference it.");
        } catch (Exception e) {
            return ctx.ack(":x: Failed to delete quizlet: " + e.getMessage());
        }
    }

    private void deliverFirstQuestions(MethodsClient client, SessionResponse session) throws Exception {
        if (session.currentQuestion() == null) {
            return;
        }
        for (ParticipantResponse participant : session.participants()) {
            messenger.sendQuestion(client, participant.slackUserId(), session.id(), session.currentQuestion());
        }
    }

    private static String afterFirstWord(String text) {
        int space = text.indexOf(' ');
        return space < 0 ? "" : text.substring(space + 1).trim();
    }

    private static String usage() {
        return "*AI Quizlet — available commands*\n"
                + "`/quiz create <topic>` — generate a new quiz (`| <questionCount>` to override the default of 5)\n"
                + "`/quiz list` — see all available quizzes\n"
                + "`/quiz delete <quizletId>` — remove a quiz that's never been started\n"
                + "`/quiz start <quizletId> @user1 @user2 ...` — start it for a group\n"
                + "`/quiz progress <sessionId>` — see everyone's score\n"
                + "`/quiz help` — show this message";
    }
}
