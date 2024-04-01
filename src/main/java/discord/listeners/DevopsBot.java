package discord.listeners;

import data.api.github.Issue;
import data.api.github.Tree;
import discord.helpers.DevopsBotHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zgamelogic.annotations.DiscordController;
import com.zgamelogic.annotations.DiscordMapping;
import com.zgamelogic.annotations.EventProperty;
import data.api.github.payloads.LabelsPayload;
import data.api.github.User;
import data.database.github.Issue.GithubIssueRepository;
import data.database.github.repository.GithubRepository;
import data.database.github.user.GithubUser;
import data.database.github.user.GithubUserRepository;
import data.database.github.workflow.WorkflowRepository;
import discord.utils.EmbedMessageGenerator;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.forums.BaseForumTag;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateAppliedTagsEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateArchivedEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import services.GitHubService;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static discord.helpers.DevopsBotHelper.*;

@Slf4j
@DiscordController
@RestController
public class DevopsBot {

    private final GithubRepository gitHubRepositories;
    private final WorkflowRepository workflowRepository;
    private final GithubIssueRepository githubIssueRepository;
    private final GithubUserRepository githubUserRepository;
    private final ObjectMapper mapper;
    private final GitHubService gitHubService;
    private final List<String> githubPropertyFiles;
    private Guild glacies;

    private final Set<Long> blockGithubMessage;

    @Value("${guild.id}")
    private String guildId;

    @Value("${general.id}")
    private long generalId;

    @Autowired
    public DevopsBot(GithubRepository gitHubRepositories, WorkflowRepository workflowRepository, GithubIssueRepository githubIssueRepository, GithubUserRepository githubUserRepository, GitHubService gitHubService){
        this.workflowRepository = workflowRepository;
        this.gitHubRepositories = gitHubRepositories;
        this.githubIssueRepository = githubIssueRepository;
        this.githubUserRepository = githubUserRepository;
        this.gitHubService = gitHubService;
        githubPropertyFiles = new ArrayList<>();
        new Thread(this::fiveMinuteUpdate, "Fetch github properties").start();
        mapper = new ObjectMapper();
        blockGithubMessage = new HashSet<>();
    }

    @DiscordMapping
    private void ready(ReadyEvent event){
        glacies = event.getJDA().getGuildById(guildId);
    }

    @DiscordMapping(Id = "github", SubId = "create_issue")
    private void addIssueCommand(SlashCommandInteractionEvent event){
        gitHubRepositories.getByGeneralId(event.getChannel().getIdLong()).ifPresentOrElse(channel -> event.replyModal(
            Modal.create("add_issue_modal", "Add github issue")
                .addActionRow(TextInput.create("issue_title", "Title", TextInputStyle.SHORT).setRequired(true).build())
                .addActionRow(TextInput.create("issue_desc", "Description", TextInputStyle.PARAGRAPH).setRequired(true).build())
                .build()
        ).queue(), () -> event.reply("This channel is not linked to a repository. Make sure you are in a general channel for the repository.").setEphemeral(true).queue());
    }

    @DiscordMapping(Id = "add_issue_modal")
    private void addIssueModal(
            ModalInteractionEvent event,
            @EventProperty(name = "issue_title") String title,
            @EventProperty(name = "issue_desc") String desc
    ){
        gitHubRepositories.getByGeneralId(event.getChannelIdLong()).ifPresent(gitRepo -> {
            Optional<GithubUser> user = githubUserRepository.getGithubUserByDiscordId(event.getUser().getIdLong());
            Issue created = gitHubService.createIssue(title, desc, gitRepo.getUrlFriendlyName(), user.map(GithubUser::getGithubToken).orElse(null));
            if(created != null){
                event.replyEmbeds(
                        EmbedMessageGenerator.githubCreatedIssue(created)
                ).queue();
            } else {
                event.reply("Unable to create issue.").setEphemeral(true).queue();
            }
        });
    }

    @DiscordMapping
    private void closedIssue(ChannelUpdateArchivedEvent event){
        if(event.getNewValue()){
            githubIssueRepository.getGithubIssueByForumPostId(event.getChannel().getIdLong()).ifPresent(githubIssue ->
                    gitHubRepositories.getByForumChannelId(event.getChannel().getIdLong()).ifPresent(gitRepo ->
                            gitHubService.closeIssue(gitRepo.getUrlFriendlyName(), githubIssue.getId(), null)
                    )
            );
        }
    }

    @DiscordMapping
    private void userMessage(MessageReceivedEvent event){
        if(!event.isFromThread() || event.getAuthor().isBot()) return;
        long forumChannelId = event.getChannel().asThreadChannel().getParentChannel().asForumChannel().getIdLong();
        long threadChannelId = event.getChannel().getIdLong();
        githubIssueRepository.getGithubIssueByForumPostId(threadChannelId).ifPresent(githubIssue ->
                gitHubRepositories.getByForumChannelId(forumChannelId).ifPresent(gitRepo -> {
                    String message = event.getMessage().getContentDisplay() + "\nThis message was posted from discord by: " + event.getAuthor().getName();
                    Optional<GithubUser> user = githubUserRepository.getGithubUserByDiscordId(event.getAuthor().getIdLong());
                    user.ifPresent(u -> blockGithubMessage.add(u.getGithubId()));
                    gitHubService.postIssueComment(
                            gitRepo.getUrlFriendlyName(),
                            githubIssue.getNumber(),
                            user.map(GithubUser::getGithubToken).orElse(null),
                            message
                    );
        }));
    }

    @DiscordMapping(Id = "github", SubId = "add_token")
    private void addGithubToken(SlashCommandInteractionEvent event, @EventProperty String token){
        User githubUser = gitHubService.getGithubAuthenticatedUser(token);
        if(githubUser == null){
            event.reply("We were unable to authenticate with that github token. Please check your access token and try again.").setEphemeral(true).queue();
            return;
        }
        githubUserRepository.getGithubUserByDiscordId(event.getUser().getIdLong()).ifPresentOrElse(gdUser -> {
            gdUser.setGithubToken(token);
            githubUserRepository.save(gdUser);
        }, () -> {
            GithubUser newGitUser = new GithubUser(event.getUser().getIdLong(), githubUser.getId(), token);
            githubUserRepository.save(newGitUser);
        });
        event.reply("Linked github token to this account").setEphemeral(true).queue();
    }

    @DiscordMapping(Id = "spring", SubId = "properties", FocusedOption = "project")
    private void propertiesProjectAutoComplete(CommandAutoCompleteInteractionEvent event){
        List<Command.Choice> files = githubPropertyFiles.stream()
                .map(path -> path.split("/")[0])
                .distinct()
                .filter(file -> file.contains(event.getFocusedOption().getValue()))
                .sorted()
                .map(file -> {
                    String[] paths = file.split("/");
                    return new Command.Choice(paths[paths.length - 1], file);
                }).toList();
        event.replyChoices(files).queue();
    }

    @DiscordMapping(Id = "spring", SubId = "properties", FocusedOption = "env")
    private void propertiesEnvAutoComplete(
            CommandAutoCompleteInteractionEvent event,
            @EventProperty String project,
            @EventProperty String env
    ) {
        if(project == null || project.isEmpty()){
            event.replyChoices().queue();
            return;
        }
        List<Command.Choice> envs = githubPropertyFiles.stream()
                .filter(file -> file.split("/")[0].contains(project))
                .filter(enviro -> enviro.split("/")[1].contains(env))
                .sorted()
                .map(e -> new Command.Choice(e.split("/")[1].split("-")[1].replace(".properties", ""), e)).toList();
        event.replyChoices(envs).queue();
    }

    @DiscordMapping(Id = "spring", SubId = "properties")
    private void springProperties(
            SlashCommandInteractionEvent event,
            @EventProperty String project,
            @EventProperty String env
    ){
        event.deferReply().setEphemeral(true).queue();
        String content = gitHubService.getPropertiesFileContent(env);
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(project);
        eb.setDescription("```\n" + content + "\n```");
        event.getHook().sendMessageEmbeds(eb.build()).setEphemeral(true).queue();
    }

    @Scheduled(cron = "0 */5 * * * *")
    private void fiveMinuteUpdate(){
        githubPropertyFiles.clear();
        githubPropertyFiles.addAll(gitHubService.getPropertiesFileList().stream().map(Tree::getPath).toList());
    }

    @DiscordMapping
    private void forumTagUpdate(ChannelUpdateAppliedTagsEvent event){
        long forumChannelId = event.getChannel().asThreadChannel().getParentChannel().asForumChannel().getIdLong();
        long threadChannelId = event.getChannel().getIdLong();
        githubIssueRepository.getGithubIssueByForumPostId(threadChannelId).ifPresent(githubIssue ->
                gitHubRepositories.getByForumChannelId(forumChannelId).ifPresent(gitRepo -> {
                    List<String> tags = event.getNewTags().stream().map(BaseForumTag::getName).toList();
                    gitHubService.editIssueLabels(gitRepo.getRepoName().replace(" ", "-"), githubIssue.getNumber(), new LabelsPayload(tags));
        }));
    }

    @GetMapping("health")
    private String healthCheck(){
        return "Healthy";
    }

    @PostMapping("github")
    private void gitHub(
            @RequestHeader(name = "X-GitHub-Event") String gitHubEvent,
            @RequestBody String body
    ) {
        HashMap<Class<?>, Object> paramMap = new HashMap<>();
        paramMap.put(String.class, body);
        paramMap.put(GithubRepository.class, gitHubRepositories);
        paramMap.put(WorkflowRepository.class, workflowRepository);
        paramMap.put(GithubIssueRepository.class, githubIssueRepository);
        paramMap.put(Guild.class, glacies);
        paramMap.put(GitHubService.class, gitHubService);
        paramMap.put(long.class, generalId);
        paramMap.put(HashSet.class, blockGithubMessage);

        for(Method method: DevopsBotHelper.class.getDeclaredMethods()){
            if(method.isAnnotationPresent(GithubEvent.class)){
                GithubEvent annotation = method.getAnnotation(GithubEvent.class);
                try {
                    // check if this is the right event
                    if(!annotation.value().equals(gitHubEvent)) continue;
                    // check again if this is the right more specific event
                    if (!annotation.action().isEmpty() && !annotation.action().equals(
                            new JSONObject(body).getString("action")
                    )) continue;

                    method.setAccessible(true);
                    LinkedList<Object> params = new LinkedList<>();
                    for(Class<?> type: method.getParameterTypes()) {
                        if(paramMap.containsKey(type)) {
                            params.add(paramMap.get(type));
                        } else {
                            params.add(mapper.readValue(body, type));
                        }
                    }
                    method.invoke(DevopsBotHelper.class, params.toArray());
                } catch (IllegalAccessException | InvocationTargetException | JSONException | JsonProcessingException e) {
                    log.error("Unable to run gitHub method", e);
                }
            }
        }
    }

    @Bean
    private List<CommandData> githubCommands(){
        return List.of(
            Commands.slash("spring", "Spring commands").addSubcommands(
                new SubcommandData("properties", "Spring properties")
                        .addOption(OptionType.STRING, "project", "Project to get the properties of", true, true)
                        .addOption(OptionType.STRING, "env", "Environment to get the properties of", true, true)
            ),
            Commands.slash("github", "Github related commands").addSubcommands(
                new SubcommandData("add_token", "Add a github token to make comments under your user on issues you comment on in discord.")
                        .addOption(OptionType.STRING, "token", "Github token with repo access", true)
            )
        );
    }
}