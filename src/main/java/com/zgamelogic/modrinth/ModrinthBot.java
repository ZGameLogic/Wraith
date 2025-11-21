package com.zgamelogic.modrinth;

import com.zgamelogic.discord.annotations.DiscordController;
import com.zgamelogic.discord.annotations.DiscordExceptionHandler;
import com.zgamelogic.discord.annotations.DiscordMapping;
import com.zgamelogic.discord.annotations.EventProperty;
import com.zgamelogic.discord.data.Model;
import com.zgamelogic.discord.services.IronWood;
import com.zgamelogic.modrinth.database.ModrinthRecord;
import com.zgamelogic.modrinth.database.ModrinthRepository;
import com.zgamelogic.modrinth.dto.ModrinthNotification;
import com.zgamelogic.modrinth.dto.ModrinthProject;
import com.zgamelogic.modrinth.dto.ModrinthVersion;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@DiscordController
public class ModrinthBot {
    private final ModrinthService modrinthService;
    private final ModrinthRepository modrinthRepository;
    private final IronWood ironWood;
    private final TransactionTemplate transactionTemplate;

    private JDA bot;

    public ModrinthBot(ModrinthService modrinthService, ModrinthRepository modrinthRepository, IronWood ironWood, PlatformTransactionManager transactionManager, PlatformTransactionManager transactionManager1) {
        this.modrinthService = modrinthService;
        this.modrinthRepository = modrinthRepository;
        this.ironWood = ironWood;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @DiscordMapping
    private void onReady(ReadyEvent event) {
        bot = event.getJDA();
        checkNotifications();
    }

    @DiscordMapping(Id = "modrinth", SubId = "follow", Document = "modrinth-follow")
    private void followSlashCommand(
        SlashCommandInteractionEvent event,
        @EventProperty(name = "project-id") String projectId,
        Model model
    ) {
        event.deferReply().queue();
        try {
            modrinthService.followProject(projectId);
        } catch (HttpClientErrorException.BadRequest e){
            if(!e.getMessage().contains("You are already following this project"))
                throw e;
        }
        ModrinthProject project = modrinthService.getProject(projectId);
        model.addContext("project", project);
        modrinthRepository.save(new ModrinthRecord(project.getId(), event.getChannelIdLong(), project.getTitle()));
    }

    @DiscordMapping(Id = "modrinth", SubId = "unfollow", FocusedOption = "project")
    private void unfollowProjectAutocomplete(CommandAutoCompleteInteractionEvent event, @EventProperty String project) {
        List<ModrinthRecord> foundProjects = modrinthRepository.findAllByChannelId(event.getChannelIdLong());
        event.replyChoices(foundProjects.stream()
            .filter(p -> project == null || p.getProjectName().toLowerCase().contains(project.toLowerCase()))
            .map(p -> new Command.Choice(p.getProjectName(), p.getProjectId()))
            .toList()
        ).queue();
    }

    @DiscordMapping(Id = "modrinth", SubId = "unfollow", Document = "modrinth-unfollow")
    private void unfollowSlashCommand(
        SlashCommandInteractionEvent event,
        @EventProperty(name = "project") String projectId,
        Model model
    ) {
        event.deferReply().queue();
        transactionTemplate.execute(status -> {
            modrinthRepository.deleteByProjectIdAndChannelId(projectId, event.getChannelIdLong());
            if(!modrinthRepository.existsByProjectId(projectId))
                modrinthService.unFollowProject(projectId);
            return null;
        });
        model.addContext("project", modrinthService.getProject(projectId));
    }

    @DiscordMapping(Id = "modrinth", SubId = "list", Document = "modrinth-list")
    private void listSlashCommand(SlashCommandInteractionEvent event, Model model) {
        String desc = modrinthRepository.findAllByChannelId(event.getChannelIdLong()).stream().map(ModrinthRecord::getProjectName).collect(Collectors.joining("\n"));
        model.addContext("description", desc);
    }

    @DiscordExceptionHandler(HttpClientErrorException.BadRequest.class)
    private void handleHttpClientErrorException(SlashCommandInteractionEvent event, HttpClientErrorException exception) {
        if(event.isAcknowledged()){
            event.getHook().sendMessage(exception.getMessage()).setEphemeral(true).queue();
        } else {
            event.reply(exception.getMessage()).setEphemeral(true).queue();
        }
    }

    @Scheduled(cron = "0 */5 * * * *")
    private void checkNotifications() {
        List<ModrinthNotification> notifications = modrinthService.getNotifications().stream()
            .filter(n -> n.getType().equals("project_update"))
            .toList();
        for (ModrinthNotification notification : notifications) {
            String projectId = notification.getBody().getProjectId();
            String versionId = notification.getBody().getVersionId();
            try {
                ModrinthVersion modrinthVersion = modrinthService.getVersion(versionId);
                ModrinthProject project = modrinthService.getProject(projectId);
                Model model = new Model();
                model.addContext("project", project);
                model.addContext("version", modrinthVersion);
                MessageEmbed embed = ironWood.generate("modrinth-update", model);
                modrinthRepository.findAllByProjectId(projectId).forEach(projectRecord ->
                    bot.getTextChannelById(projectRecord.getChannelId()).sendMessageEmbeds(embed).queue()
                );
                modrinthService.deleteNotification(notification.getId());
            } catch(Exception e){
                log.error("Unable to process modrinth notification", e);
            }
        }
    }

    @Bean
    public List<CommandData> modrinthCommands(){
        return List.of(
            Commands.slash("modrinth", "Modrinth API listening").addSubcommands(
                new SubcommandData("follow", "Follow a modrinth project to get updates in this channel")
                    .addOption(OptionType.STRING, "project-id", "The id of the project", true),
                new SubcommandData("unfollow", "Unfollow a modrinth project for this channel")
                    .addOption(OptionType.STRING, "project", "The project id to stop listening to", true, true),
                new SubcommandData("list", "List the followed projects in this channel")
            )
        );
    }
}

