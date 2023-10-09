package bot.utils;

import bot.listener.CurseForgeBot;
import data.api.github.events.PushEvent;
import data.api.monitor.Monitor;
import data.database.curseforge.CurseforgeRecord;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.awt.*;
import java.time.Instant;
import java.util.LinkedList;

public abstract class EmbedMessageGenerator {

    private final static Color GENERAL_COLOR = new Color(99, 42, 129);
    private final static Color CURSEFORGE_COLOR = new Color(239, 99, 54);
    private final static Color GITHUB_COLOR = new Color(4, 6, 10);

    private final static Color DATA_DOG_OK_COLOR = new Color(64, 194, 99);
    private final static Color DATA_DOG_ALERT_COLOR = new Color(233, 54, 74);

    private final static String DATA_DOG_OK = ":green_square:";
    private final static String DATA_DOG_ALERT = ":red_square:";

    public static MessageEmbed gitHubPush(PushEvent event){
        EmbedBuilder eb = new EmbedBuilder();

        String ref = event.getRef();
        String branch = ref.substring(ref.lastIndexOf("/") + 1);

        eb.setColor(GITHUB_COLOR);
        eb.setTitle(event.getSender().getLogin() + " pushed to " + branch, event.getHeadCommit().getUrl());
        eb.setThumbnail(event.getSender().getAvatar_url());
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

        eb.setFooter(event.getSender().getLogin(), event.getSender().getAvatar_url());
        eb.setTimestamp(Instant.now());
        return eb.build();
    }

    public static MessageEmbed monitorStatus(LinkedList<Monitor> monitors){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Monitors status");
        StringBuilder desc = new StringBuilder();

        boolean good = true;

        for(Monitor m: monitors){
            if(!m.getStatus()) good = false;
            if(m.getType().equals("minecraft")){
                desc.append(m.getStatus() ? DATA_DOG_OK : DATA_DOG_ALERT).append(": ").append(m.getName()).append("\n");
                if(m.getStatus() && m.getOnline() > 0) {
                    desc.append("**Online: **").append(m.getOnline()).append("\n");
                    LinkedList<String> players = new LinkedList<>(m.getOnlinePlayers());
                    players.sort(String::compareTo);
                    for (String name : players) {
                        desc.append("-").append(name).append("\n");
                    }
                }
            } else {
                desc.append(m.getStatus() ? DATA_DOG_OK : DATA_DOG_ALERT).append(": ").append(m.getName()).append("\n");
            }
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

    public static MessageEmbed curseforgeUpdate(CurseForgeBot.CurseforgeProject project, Boolean mentionable){
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
