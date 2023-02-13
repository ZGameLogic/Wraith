package bot.utils;

import application.App;
import bot.listener.CurseForgeBot;
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

    private final static Color GENERAL_COLOR = new Color(99, 42, 129);
    private final static Color CURSEFORGE_COLOR = new Color(239, 99, 54);
    private final static Color ATLASSIAN_COLOR = new Color(13, 71, 161);

    public static MessageEmbed jiraIssueCreated(JSONObject json) throws JSONException {
        String summary = json.getJSONObject("issue").getJSONObject("fields").getString("summary");
        String key = json.getJSONObject("issue").getString("key");
        String issueUrl = App.config.getJiraURL() + "browse/" + key;
        String desc = json.getJSONObject("issue").getJSONObject("fields").getString("description");
        String createdBy = json.getJSONObject("user").getString("name");
        String type = json.getJSONObject("issue").getJSONObject("fields").getJSONObject("issuetype").getString("name");
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(ATLASSIAN_COLOR);
        eb.setTitle("Issue created: " + key, issueUrl);
        eb.setDescription(
                "**" + summary + "**\nDescription: " + desc + "\nCreated by: " + createdBy
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
