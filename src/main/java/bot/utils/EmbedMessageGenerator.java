package bot.utils;

import application.App;
import bot.listener.CurseForgeBot;
import data.api.atlassian.jira.JiraAPIIssue;
import data.api.datadog.Monitor;
import data.database.curseforge.CurseforgeRecord;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.time.Instant;
import java.util.LinkedList;

public abstract class EmbedMessageGenerator {

    // private final static Color GENERAL_COLOR = new Color(99, 42, 129);
    private final static Color CURSEFORGE_COLOR = new Color(239, 99, 54);
    private final static Color ATLASSIAN_COLOR = new Color(13, 71, 161);

    private final static Color DATA_DOG_OK_COLOR = new Color(64, 194, 99);
    private final static Color DATA_DOG_ALERT_COLOR = new Color(233, 54, 74);
    private final static Color DATA_DOG_NONE_COLOR = new Color(129, 138, 163);

    private final static String DATA_DOG_OK = ":green_square:";
    private final static String DATA_DOG_ALERT = ":red_square:";
    private final static String DATA_DOG_NONE = ":white_large_square:";

    public static MessageEmbed bitbucketPrMerged(JSONObject json) throws JSONException {
        String projectSlug = json.getJSONObject("pullRequest").getJSONObject("fromRef").getJSONObject("repository").getJSONObject("project").getString("key");
        String repoSlug = json.getJSONObject("pullRequest").getJSONObject("fromRef").getJSONObject("repository").getString("slug");
        long prId = json.getJSONObject("pullRequest").getLong("id");
        String url = App.config.getBitbucketURL() + "projects/" + projectSlug + "/repos/" + repoSlug + "/pull-requests/" + prId + "/overview";
        String fromBranchName = json.getJSONObject("pullRequest").getJSONObject("fromRef").getString("displayId");
        String toBranchName = json.getJSONObject("pullRequest").getJSONObject("toRef").getString("displayId");
        String repoName = json.getJSONObject("pullRequest").getJSONObject("toRef").getJSONObject("repository").getString("name");
        String prTitle = json.getJSONObject("pullRequest").getString("title");
        String author = json.getJSONObject("pullRequest").getJSONObject("author").getJSONObject("user").getString("name");
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(ATLASSIAN_COLOR);
        eb.setTitle("Pull request merged: " + fromBranchName + " -> " + toBranchName, url);
        eb.setDescription(
                "**" + prTitle + "**\n" +
                        "Author: " + author
        );
        eb.setFooter(repoName);
        eb.setTimestamp(Instant.now());
        return eb.build();
    }

    public static MessageEmbed bitbucketPrCreate(JSONObject json) throws JSONException {
        String projectSlug = json.getJSONObject("pullRequest").getJSONObject("fromRef").getJSONObject("repository").getJSONObject("project").getString("key");
        String repoSlug = json.getJSONObject("pullRequest").getJSONObject("fromRef").getJSONObject("repository").getString("slug");
        long prId = json.getJSONObject("pullRequest").getLong("id");
        String url = App.config.getBitbucketURL() + "projects/" + projectSlug + "/repos/" + repoSlug + "/pull-requests/" + prId + "/overview";
        String fromBranchName = json.getJSONObject("pullRequest").getJSONObject("fromRef").getString("displayId");
        String toBranchName = json.getJSONObject("pullRequest").getJSONObject("toRef").getString("displayId");
        String prTitle = json.getJSONObject("pullRequest").getString("title");
        String repoName = json.getJSONObject("pullRequest").getJSONObject("toRef").getJSONObject("repository").getString("name");
        String author = json.getJSONObject("pullRequest").getJSONObject("author").getJSONObject("user").getString("name");
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(ATLASSIAN_COLOR);
        eb.setTitle("Pull request created: " + fromBranchName + " -> " + toBranchName, url);
        eb.setDescription(
                "**" + prTitle + "**\n" +
                "Author: " + author
        );
        eb.setTimestamp(Instant.now());
        eb.setFooter(repoName);
        return eb.build();
    }

    public static MessageEmbed bitbucketBranchCreated(JSONObject json, int index) throws JSONException {
        String branchName = json.getJSONArray("changes").getJSONObject(index).getJSONObject("ref").getString("displayId");
        String creator = json.getJSONObject("actor").getString("name");
        String repoName = json.getJSONObject("repository").getString("name");
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(ATLASSIAN_COLOR);
        eb.setTitle("Branch created: " + branchName);
        eb.setDescription("Created by: " + creator);
        eb.setTimestamp(Instant.now());
        eb.setFooter(repoName);
        return eb.build();
    }

    public static MessageEmbed bitbucketPushMade(JSONObject json, JSONObject commit, int index) throws JSONException {
        String branch = json.getJSONArray("changes").getJSONObject(index).getJSONObject("ref").getString("displayId");
        String commitUrl = App.config.getBitbucketURL() + "projects/" +
                json.getJSONObject("repository").getJSONObject("project").getString("key") +
                "/repos/" + json.getJSONObject("repository").getString("slug") + "/commits/" +
                json.getJSONArray("changes").getJSONObject(index).getString("toHash");
        String message = commit.getString("message");
        String author = json.getJSONObject("actor").getString("name");
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(ATLASSIAN_COLOR);
        eb.setTitle("Push was made to branch: " + branch, commitUrl);
        eb.setDescription(message + "\n" + author);
        String repoName = json.getJSONObject("repository").getString("name");
        eb.setFooter(repoName);
        eb.setTimestamp(Instant.now());
        return eb.build();
    }

    public static MessageEmbed jiraIssueCommented(JSONObject json) throws JSONException {
        String key = json.getJSONObject("issue").getString("key");
        String issueUrl = App.config.getJiraURL() + "browse/" + key;
        String message = json.getJSONObject("comment").getString("body");
        String author = json.getJSONObject("comment").getJSONObject("author").getString("name");
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(ATLASSIAN_COLOR);
        eb.setTitle("Issue commented on: " + key, issueUrl);
        eb.setDescription(
                message  + "\nBy user: " + author
        );
        eb.setTimestamp(Instant.now());
        return eb.build();
    }

    public static MessageEmbed jiraIssueCreated(JSONObject json) throws JSONException {
        String summary = json.getJSONObject("issue").getJSONObject("fields").getString("summary");
        String key = json.getJSONObject("issue").getString("key");
        String issueUrl = App.config.getJiraURL() + "browse/" + key;
        String desc = json.getJSONObject("issue").getJSONObject("fields").getString("description");
        String createdBy = json.getJSONObject("user").getString("name");
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(ATLASSIAN_COLOR);
        eb.setTitle("Issue created: " + key, issueUrl);
        eb.setDescription(
                "**" + summary + "**\nDescription: " + desc + "\nCreated by: " + createdBy
        );
        eb.setTimestamp(Instant.now());
        return eb.build();
    }

    public static MessageEmbed jiraIssueUpdated(JiraAPIIssue issue) {
        String key = issue.getIssue().getKey();
        String summary = issue.getIssue().getFields().getSummary();
        String issueUrl = App.config.getJiraURL() + "browse/" + key;
        String from = "";
        String to = "";
        for(JiraAPIIssue.Changelog.Item item: issue.getChangelog().getItems()){
            if(!item.getField().equals("status")) continue;
            from = item.getFromString();
            to = item.getToString();
        }
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(ATLASSIAN_COLOR);
        eb.setTitle("Issue updated: " + key, issueUrl);
        eb.setDescription(
                "**" + summary + "**\nIssue was moved from **" + from + "** to **" + to + "**."
        );
        eb.setTimestamp(Instant.now());
        return eb.build();
    }

    public static MessageEmbed curseforgeUpdated(CurseforgeRecord record){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(CURSEFORGE_COLOR);
        eb.setTitle("Update status for: " + record.getName());
        eb.setDescription("This project was last updated: " + TimeFormat.DATE_TIME_SHORT.format(record.getLastUpdated().getTime()) + "\n" +
                "This project was last checked: " + TimeFormat.DATE_TIME_SHORT.format(record.getLastChecked().getTime()));
        return eb.build();
    }

    public static MessageEmbed curseforgeList(LinkedList<CurseForgeBot.CurseforgeProject> projects){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(CURSEFORGE_COLOR);
        eb.setTitle("Total projects: " + projects.size());
        StringBuilder desc = new StringBuilder();
        for(CurseForgeBot.CurseforgeProject p: projects){
            desc.append(p.getName()).append("\n");
        }
        eb.setDescription(desc.toString());
        return eb.build();
    }

    public static MessageEmbed curseforgeInitial(CurseForgeBot.CurseforgeProject project){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(CURSEFORGE_COLOR);
        eb.setTitle("Listening to " + project.getName(), project.getUrl());
        eb.setDescription(project.getSummary() + "\nDownloads: " + project.getDownloadCount());
        eb.setThumbnail(project.getLogoUrl());
        eb.addField("Current file", project.getFileName(), true);
        return eb.build();
    }

    public static MessageEmbed curseforgeUpdate(CurseForgeBot.CurseforgeProject project){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(CURSEFORGE_COLOR);
        eb.setTitle("File update for " + project.getName(), project.getUrl());
        eb.setDescription(project.getFileName());
        if(!project.getServerFileName().equals("")){
            eb.addField("Server file", project.getServerFileName() + "\n" + project.getServerFileUrl(), true);
        }
        eb.setThumbnail(project.getLogoUrl());
        return eb.build();
    }
}
