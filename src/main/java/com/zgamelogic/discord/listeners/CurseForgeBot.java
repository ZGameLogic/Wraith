package com.zgamelogic.discord.listeners;

import com.zgamelogic.discord.utils.EmbedMessageGenerator;
import com.zgamelogic.annotations.Bot;
import com.zgamelogic.annotations.DiscordController;
import com.zgamelogic.annotations.DiscordMapping;
import com.zgamelogic.data.api.curseforge.CurseforgeMod;
import com.zgamelogic.data.database.curseforge.CurseforgeRecord;
import com.zgamelogic.data.database.curseforge.CurseforgeRepository;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RestController;
import com.zgamelogic.services.CurseforgeService;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RestController
@DiscordController
public class CurseForgeBot {

    private final CurseforgeRepository checks;
    private final CurseforgeService curseforgeService;

    @Bot
    private JDA bot;

    @Autowired
    public CurseForgeBot(CurseforgeRepository checks, CurseforgeService curseforgeService) {
        this.checks = checks;
        this.curseforgeService = curseforgeService;
    }

    @DiscordMapping
    public void ready(ReadyEvent event) {
        new Thread(this::update, "Curseforge-startup").start();
        curseforgeService.getCurseforgeMods(925200, 268560, 223794).forEach(mod -> {
            log.info("Mod name: {}", mod.getName());
        });
    }

    @DiscordMapping(SubId = "updated", Id = "curseforge", FocusedOption = "project")
    private void autoCompleteProject(CommandAutoCompleteInteractionEvent event){
        LinkedList<String> projects = new LinkedList<>();
        for(CurseforgeRecord record: checks.getProjectsByGuildAndChannel(event.getGuild().getIdLong(), event.getChannel().getIdLong())){
            projects.add(record.getName());
        }
        String[] words = projects.toArray(new String[0]);
        List<Command.Choice> options = Stream.of(words)
                .filter(word -> word.startsWith(event.getFocusedOption().getValue()))
                .map(word -> new Command.Choice(word, word))
                .collect(Collectors.toList());
        event.replyChoices(options).queue();
    }

    @DiscordMapping(Id = "curseforge", SubId = "list")
    private void list(SlashCommandInteractionEvent event){
        event.deferReply().queue();
        LinkedList<CurseforgeMod> projects = new LinkedList<>();
        for(CurseforgeRecord record: checks.getProjectsByGuildAndChannel(event.getGuild().getIdLong(), event.getChannel().getIdLong())){
            projects.add(curseforgeService.getCurseforgeMod(Long.parseLong(record.getProjectId())));
        }
        event.getHook().sendMessageEmbeds(EmbedMessageGenerator.curseforgeList(projects)).queue();
    }

    @DiscordMapping(Id = "curseforge", SubId = "updated")
    private void updated(SlashCommandInteractionEvent event){
        String projectName = event.getOption("project").getAsString();
        Optional<CurseforgeRecord> project = checks.getProjectByName(projectName);
        if(project.isPresent()){
            event.replyEmbeds(EmbedMessageGenerator.curseforgeUpdated(project.get())).queue();
        } else {
            event.reply("Cannot find a project with that name").setEphemeral(true).queue();
        }
    }

    @DiscordMapping(Id = "curseforge", SubId = "listen")
    private void follow(SlashCommandInteractionEvent event){
        event.deferReply().queue();
        String project = event.getOption("project").getAsString();
        CurseforgeRecord cfr = new CurseforgeRecord();
        cfr.setChannelId(event.getChannel().getIdLong());
        cfr.setGuildId(event.getGuild().getIdLong());
        cfr.setLastChecked(new Date());
        cfr.setLastUpdated(new Date());
        cfr.setMentionable(
                event.getOption("mention") != null && event.getOption("mention").getAsBoolean()
        );
        cfr.setProjectId(project);
        CurseforgeMod response = curseforgeService.getCurseforgeMod(Long.parseLong(project));
        if(!response.isValid()){
            event.getHook().sendMessage("No project with that ID found").queue();
            return;
        }
        cfr.setName(response.getName());
        cfr.setProjectVersionId(response.getMainFileId() + "");
        event.getHook().sendMessageEmbeds(EmbedMessageGenerator.curseforgeInitial(response)).queue();
        checks.save(cfr);
    }

    @DiscordMapping(Id = "curseforge", SubId = "forget")
    private void forget(SlashCommandInteractionEvent event){
        String project = event.getOption("project").getAsString();
        Optional<CurseforgeRecord> dbProject = checks.getProjectById(project, event.getGuild().getIdLong(), event.getChannel().getIdLong());
        if(dbProject.isPresent()){
            checks.deleteById(dbProject.get().getId());
            event.reply("No longer watching this project").queue();
            return;
        }
        event.reply("No project with that ID is being followed").queue();
    }

    @Scheduled(cron = "0 */5 * * * *")
    private void fiveMinuteTask() {
        update();
    }

    public void update(){
        log.info("Updating curseforge stuff");
        for(CurseforgeRecord check: checks.findAll()){
            try {
                CurseforgeMod current = curseforgeService.getCurseforgeMod(Long.parseLong(check.getProjectId()));
                if (check.getProjectVersionId() == null || !check.getProjectVersionId().equals(current.getMainFileId() + "")) {
                    Boolean mention = check.getMentionable();
                    MessageCreateAction message = bot.getGuildById(check.getGuildId()).getTextChannelById(check.getChannelId()).sendMessageEmbeds(
                            EmbedMessageGenerator.curseforgeUpdate(current, mention)
                    );
                    message.queue();
                    check.setProjectVersionId(current.getMainFileId() + "");
                    check.setLastUpdated(new Date());
                    check.setName(current.getName());
                    log.info("Project: " + check.getProjectId() + "\tOld file: " + check.getProjectVersionId() + "\tNew file: " + current.getMainFileId());
                }
                check.setLastChecked(new Date());
                check.setName(current.getName());
                checks.save(check);
            } catch (Exception e){
                log.error("Error updating curseforge check", e);
            }
        }
    }

    @Bean
    private CommandData curseforgeCommands(){
        return Commands.slash("curseforge", "Slash command for curseforge related things")
            .addSubcommands(
                new SubcommandData("listen", "Listens to a project")
                        .addOption(OptionType.STRING, "project", "Project to watch", true)
                        .addOption(OptionType.BOOLEAN, "mention", "Get mentioned when this updates", false),
                new SubcommandData("forget", "Stops listening to a project")
                        .addOption(OptionType.STRING, "project", "Project to watch", true),
                new SubcommandData("list", "Lists all the projects currently followed in this channel"),
                new SubcommandData("updated", "Shows when the project was last updated")
                        .addOption(OptionType.STRING, "project", "Project to check", true, true)
            );
    }
}
