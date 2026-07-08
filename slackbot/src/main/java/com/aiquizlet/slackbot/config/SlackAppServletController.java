package com.aiquizlet.slackbot.config;

import com.slack.api.bolt.App;
import com.slack.api.bolt.jakarta_servlet.SlackAppServlet;
import jakarta.servlet.annotation.WebServlet;

/**
 * Single request URL for everything Slack sends us: slash commands and interactive
 * component (block action) payloads. Configure this exact path — {@code /slack/events}
 * — as the Request URL for both Slash Commands and Interactivity in the Slack app
 * dashboard (see slackbot/README.md).
 */
@WebServlet("/slack/events")
public class SlackAppServletController extends SlackAppServlet {

    public SlackAppServletController(App app) {
        super(app);
    }
}
