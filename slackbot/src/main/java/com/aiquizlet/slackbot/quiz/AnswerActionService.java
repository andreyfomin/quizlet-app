package com.aiquizlet.slackbot.quiz;

import com.aiquizlet.slackbot.backend.AnswerResultResponse;
import com.aiquizlet.slackbot.backend.AnswerReviewResponse;
import com.aiquizlet.slackbot.backend.BackendClient;
import com.aiquizlet.slackbot.backend.ParticipantReviewResponse;
import com.slack.api.app_backend.interactive_components.payload.BlockActionPayload;
import com.slack.api.bolt.context.builtin.ActionContext;
import com.slack.api.bolt.request.builtin.BlockActionRequest;
import com.slack.api.bolt.response.Response;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Handles a click on one of the option buttons from {@link SlackBlocks}. The button's
 * value is "{sessionId}:{optionIndex}"; the answering user's Slack id, and the
 * channel/message to update in place, all come from the block action payload itself.
 */
@Service
public class AnswerActionService {

    private final BackendClient backendClient;
    private final QuizMessenger messenger;

    public AnswerActionService(BackendClient backendClient, QuizMessenger messenger) {
        this.backendClient = backendClient;
        this.messenger = messenger;
    }

    public Response handle(BlockActionRequest req, ActionContext ctx) {
        BlockActionPayload payload = req.getPayload();
        try {
            BlockActionPayload.Action action = payload.getActions().get(0);
            String[] value = action.getValue().split(":", 2);
            Long sessionId = Long.parseLong(value[0]);
            int selectedOptionIndex = Integer.parseInt(value[1]);

            String slackUserId = payload.getUser().getId();
            String channelId = payload.getChannel().getId();
            String messageTs = payload.getMessage().getTs();

            AnswerResultResponse result = backendClient.submitAnswer(sessionId, slackUserId, selectedOptionIndex);
            String feedback = (result.correct() ? ":white_check_mark: Correct!" : ":x: Not quite.")
                    + " Score: " + result.score();

            if (result.nextQuestion() != null) {
                messenger.updateWithNextQuestion(ctx.client(), channelId, messageTs, sessionId, feedback,
                        result.nextQuestion());
            } else {
                ParticipantReviewResponse review = backendClient.getReview(sessionId, slackUserId);
                messenger.updateWithFinalResult(ctx.client(), channelId, messageTs,
                        feedback + "\n" + formatReview(review));
            }
        } catch (Exception e) {
            ctx.getLogger().warn("Failed to process quiz answer", e);
        }
        return ctx.ack();
    }

    private static String formatReview(ParticipantReviewResponse review) {
        StringBuilder sb = new StringBuilder(":checkered_flag: *Quiz complete! Final score: "
                + review.score() + "/" + review.totalQuestions() + " correct*\n\n");

        List<AnswerReviewResponse> answers = review.answers();
        for (int i = 0; i < answers.size(); i++) {
            AnswerReviewResponse a = answers.get(i);
            sb.append(i + 1).append(". ")
                    .append(a.correct() ? ":white_check_mark:" : ":x:")
                    .append(" ").append(a.questionText()).append("\n");
            sb.append("   Your answer: ").append(SlackBlocks.optionLabel(a.selectedOptionIndex()))
                    .append(". ").append(a.options().get(a.selectedOptionIndex())).append("\n");
            if (!a.correct()) {
                sb.append("   Correct answer: ").append(SlackBlocks.optionLabel(a.correctOptionIndex()))
                        .append(". ").append(a.options().get(a.correctOptionIndex())).append("\n");
            }
        }
        return sb.toString();
    }
}
