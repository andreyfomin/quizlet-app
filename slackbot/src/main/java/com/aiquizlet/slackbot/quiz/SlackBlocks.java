package com.aiquizlet.slackbot.quiz;

import com.aiquizlet.slackbot.backend.QuestionPublicResponse;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.element.BlockElement;

import java.util.ArrayList;
import java.util.List;

import static com.slack.api.model.block.Blocks.actions;
import static com.slack.api.model.block.Blocks.asBlocks;
import static com.slack.api.model.block.Blocks.section;
import static com.slack.api.model.block.composition.BlockCompositions.markdownText;
import static com.slack.api.model.block.composition.BlockCompositions.plainText;
import static com.slack.api.model.block.element.BlockElements.asElements;
import static com.slack.api.model.block.element.BlockElements.button;

/**
 * Builds the Block Kit messages the bot sends for a quiz question. Each option button
 * gets its own action_id ({@link #ANSWER_ACTION_ID_PREFIX} + the option index) —
 * Slack's {@code chat.postMessage} rejects a message outright ({@code invalid_blocks})
 * if two interactive elements in the same block share an action_id, so all four
 * buttons using one constant id (an earlier version of this class) doesn't just risk
 * misrouted clicks, it fails to send at all. {@code AnswerActionService} matches the
 * handler by pattern ({@link #ANSWER_ACTION_ID_PATTERN}) rather than exact string for
 * this reason. The button's value still carries the session id and option index
 * ("{sessionId}:{optionIndex}") — the action_id only needs to be unique, its content
 * isn't otherwise meaningful.
 */
public final class SlackBlocks {

    public static final String ANSWER_ACTION_ID_PREFIX = "quiz_answer_";
    public static final String ANSWER_ACTION_ID_PATTERN = "^" + ANSWER_ACTION_ID_PREFIX + "\\d+$";

    private static final String[] LABELS = {"A", "B", "C", "D", "E", "F"};

    private SlackBlocks() {
    }

    /** First time a question is shown to a participant. */
    public static List<LayoutBlock> question(Long sessionId, QuestionPublicResponse question) {
        return question(sessionId, question, null);
    }

    /** A question shown together with feedback on the previous answer. */
    public static List<LayoutBlock> question(Long sessionId, QuestionPublicResponse question, String headerText) {
        List<LayoutBlock> blocks = new ArrayList<>();
        if (headerText != null) {
            blocks.add(section(s -> s.text(markdownText(headerText))));
        }
        blocks.add(section(s -> s.text(markdownText("*" + question.text() + "*"))));
        blocks.add(actions(a -> a.elements(optionButtons(sessionId, question))));
        return blocks;
    }

    /** Terminal message once a participant has answered every question. */
    public static List<LayoutBlock> finalResult(String text) {
        return asBlocks(section(s -> s.text(markdownText(text))));
    }

    private static List<BlockElement> optionButtons(Long sessionId, QuestionPublicResponse question) {
        List<String> options = question.options();
        List<BlockElement> buttons = new ArrayList<>();
        for (int i = 0; i < options.size(); i++) {
            int optionIndex = i;
            String optionText = options.get(optionIndex);
            buttons.add(button(b -> b
                    .actionId(ANSWER_ACTION_ID_PREFIX + optionIndex)
                    .text(plainText(optionLabel(optionIndex) + ". " + optionText))
                    .value(sessionId + ":" + optionIndex)));
        }
        return asElements(buttons.toArray(new BlockElement[0]));
    }

    /** "A", "B", "C", ... for the first 6 options, then "7", "8", ... beyond that. */
    public static String optionLabel(int optionIndex) {
        return optionIndex < LABELS.length ? LABELS[optionIndex] : String.valueOf(optionIndex + 1);
    }
}
