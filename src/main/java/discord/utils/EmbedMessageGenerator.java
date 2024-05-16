package discord.utils;

import data.api.curseforge.CurseforgeMod;
import data.api.dataOtter.Monitor;
import data.api.github.*;
import data.api.github.events.PublishReleasedEvent;
import data.api.github.events.PushEvent;
import data.api.zGameLogic.SOTData;
import data.database.curseforge.CurseforgeRecord;
import data.discord.SeaOfThievesIslandData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.awt.*;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.List;

public abstract class EmbedMessageGenerator {

    private final static Color GENERAL_COLOR = new Color(99, 42, 129);
    private final static Color CURSEFORGE_COLOR = new Color(239, 99, 54);
    private final static Color GITHUB_COLOR = new Color(4, 6, 10);

    private final static Color DATA_DOG_OK_COLOR = new Color(64, 194, 99);
    private final static Color DATA_DOG_ALERT_COLOR = new Color(233, 54, 74);

    private final static Color SEA_OF_THIEVES_COLOR = new Color(21, 230, 154);

    private final static String DATA_DOG_OK = ":green_square:";
    private final static String DATA_DOG_ALERT = ":red_square:";
    private final static Map<Integer, Color> GITHUB_STATUS_COLOR_MAP = Map.of(
            -1, new Color(233, 54, 74),
            0, new Color(13, 71, 161),
            1, new Color(64, 194, 99)
    );

    public static MessageEmbed sotDataMessage(SOTData returnData) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(SEA_OF_THIEVES_COLOR);
        eb.setTitle("Data recorded");
        eb.appendDescription(String.format("Time for event: <t:%d:F>\n", returnData.getProposed().atZone(ZoneId.systemDefault()).toEpochSecond()));
        eb.appendDescription(String.format("\nBen: `%b`\nGreg: `%b`\nJJ: `%b`\nPatrick `%b`\n", returnData.isBen(), returnData.isGreg(), returnData.isJj(), returnData.isPatrick()));
        eb.appendDescription(String.format("Success: `%b`", returnData.isSuccess()));
        eb.setFooter("ID: " + returnData.getId());
        eb.setTimestamp(Instant.now());
        return eb.build();
    }

    public static MessageEmbed sotIslandDataMessage(SeaOfThievesIslandData data){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(SEA_OF_THIEVES_COLOR);
        eb.setTitle("Island: " + data.name());
        eb.setImage("attachment://" + data.islandPng().getName());
        eb.addField("Location", data.chords(), true);
        return eb.build();
    }

    public static MessageEmbed githubCreatedIssue(Issue issue){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(GITHUB_COLOR);
        eb.setTitle(issue.getTitle(), issue.getHtmlUrl());
        eb.setDescription(issue.getBody());
        eb.setFooter(issue.getUser().getLogin(), issue.getUser().getAvatar_url());
        eb.setTimestamp(Instant.now());
        return eb.build();
    }

    public static MessageEmbed githubPublishedReleaseMessage(PublishReleasedEvent release){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(GENERAL_COLOR);
        eb.setTitle(String.format(
                "%s release: %s",
                release.getRepository().getName(),
                release.getRelease().getName()
        ), release.getRelease().getHtml_url());
        eb.setDescription(release.getRelease().getBody());
        User releasedUser = release.getRelease().getAuthor();
        eb.setFooter(releasedUser.getLogin(), releasedUser.getAvatar_url());
        eb.setTimestamp(Instant.now());
        return eb.build();
    }

    public static MessageEmbed githubIssueCommentedMessage(Comment comment){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(GITHUB_COLOR);
        eb.setDescription("```" + comment.getBody() + "```");
        eb.setFooter(comment.getUser().getLogin(), comment.getUser().getAvatar_url());
        return eb.build();
    }

    public static MessageEmbed githubIssueReopenedMessage(User user){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(GITHUB_COLOR);
        eb.setTitle("This issue has been re-opened.");
        eb.setAuthor(user.getLogin(), user.getUrl(), user.getAvatar_url());
        return eb.build();
    }

    public static MessageEmbed githubIssueClosedMessage(User user){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(GITHUB_COLOR);
        eb.setTitle("This issue has been closed.");
        eb.setAuthor(user.getLogin(), user.getUrl(), user.getAvatar_url());
        return eb.build();
    }

    public static MessageEmbed githubIssueAssignedMessage(User assignee){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(GITHUB_COLOR);
        eb.setTitle(assignee.getLogin() + " has been assigned to this issue");
        eb.setThumbnail(assignee.getAvatar_url());
        return eb.build();
    }

    public static MessageEmbed workflow(WorkflowRun run, HashMap<String, Emoji> emojis){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(GITHUB_STATUS_COLOR_MAP.get(run.getRunStatus()));
        eb.setTitle("Github Action: " + run.getJobs().getFirst().getWorkflowName(), run.getJobs().getFirst().getHtmlUrl());
        StringBuilder desc = new StringBuilder();
        for(WorkflowJob job: run.getJobs()){
            String jobEmojiCode = job.getStatus() + " " + job.getConclusion();
            String jobEmojiPrefix = jobEmojiCode.equals("queued null") ? "a" : "";
            desc.append("<").append(jobEmojiPrefix).append(":")
                    .append((emojis.containsKey(jobEmojiCode) ? emojis.get(jobEmojiCode) : emojis.get("working")).getAsReactionCode())
                    .append("> `")
                    .append(job.getName())
                    .append("`\n");
            for(WorkflowJob.Step step: job.getSteps()){
                desc.append("<:")
                        .append(emojis.get(step.getStatus() + " " + step.getConclusion()).getAsReactionCode())
                        .append("> `    ")
                        .append(step.getName())
                        .append("`\n");
            }
        }

        eb.setDescription(desc);
        return eb.build();
    }

    public static MessageEmbed gitHubPush(PushEvent event){
        EmbedBuilder eb = new EmbedBuilder();

        String ref = event.getRef();
        String branch = ref.substring(ref.lastIndexOf("/") + 1);

        eb.setColor(GITHUB_COLOR);
        eb.setTitle("push to " + branch, event.getHeadCommit().getUrl());
        StringBuilder desc = new StringBuilder();
        desc.append("> ").append(event.getHeadCommit().getMessage());
        desc.append("\n```diff\n");
        event.getHeadCommit().getAdded().forEach(file -> {
            String[] paths = file.split("/");
            desc.append("+ ").append(paths[paths.length - 1]);
            desc.append("\n");
        });

        event.getHeadCommit().getRemoved().forEach(file -> {
            String[] paths = file.split("/");
            desc.append("- ").append(paths[paths.length - 1]);
            desc.append("\n");
        });

        event.getHeadCommit().getModified().forEach(file -> {
            String[] paths = file.split("/");
            desc.append("--- ").append(paths[paths.length - 1]);
            desc.append("\n");
        });

        desc.append("```");
        eb.setDescription(desc.toString());

        eb.setAuthor(event.getSender().getLogin(), event.getSender().getHtml_url(), event.getSender().getAvatar_url());
        eb.setTimestamp(Instant.now());
        eb.setFooter(event.getRepository().getName());
        return eb.build();
    }

    public static MessageEmbed monitorStatus(List<Monitor> monitors){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Monitors status");
        StringBuilder desc = new StringBuilder();

        boolean good = true;

        for(Monitor m: monitors){
            if(!m.getStatus().isStatus()) good = false;
            desc.append(m.getStatus().isStatus() ? DATA_DOG_OK : DATA_DOG_ALERT).append(": ").append(m.getName()).append("\n");
        }

        eb.setColor(good ? DATA_DOG_OK_COLOR : DATA_DOG_ALERT_COLOR);
        eb.setDescription(desc.toString());
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

    public static MessageEmbed curseforgeList(LinkedList<CurseforgeMod> projects){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(CURSEFORGE_COLOR);
        eb.setTitle("Total projects: " + projects.size());
        StringBuilder desc = new StringBuilder();
        for(CurseforgeMod p: projects){
            desc.append(p.getName()).append("\n");
        }
        eb.setDescription(desc.toString());
        return eb.build();
    }

    public static MessageEmbed curseforgeInitial(CurseforgeMod project){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(CURSEFORGE_COLOR);
        eb.setTitle("Listening to " + project.getName(), project.getUrl());
        eb.setDescription(project.getSummary() + "\nDownloads: " + project.getDownloadCount());
        eb.setThumbnail(project.getLogoUrl());
        eb.addField("Current file", project.getFileName(), true);
        return eb.build();
    }

    public static MessageEmbed curseforgeUpdate(CurseforgeMod project, Boolean mentionable){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(CURSEFORGE_COLOR);
        eb.setTitle("File update for " + project.getName(), project.getUrl());
        eb.setDescription(project.getFileName() + (mentionable != null && mentionable ? " <@232675572772372481>" : ""));
        if(!project.getServerFileName().isEmpty()){
            eb.addField("Server file", project.getServerFileName() + "\n" + project.getServerFileUrl(), true);
        }
        eb.setThumbnail(project.getLogoUrl());
        return eb.build();
    }
}
