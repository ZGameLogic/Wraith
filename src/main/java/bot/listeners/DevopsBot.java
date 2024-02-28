package bot.listeners;

import bot.helpers.DevopsBotHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zgamelogic.annotations.DiscordController;
import com.zgamelogic.annotations.DiscordMapping;
import com.zgamelogic.annotations.EventProperty;
import data.api.github.LabelsPayload;
import data.api.github.User;
import data.database.github.Issue.GithubIssueRepository;
import data.database.github.repository.GithubRepository;
import data.database.github.user.GithubUser;
import data.database.github.user.GithubUserRepository;
import data.database.github.workflow.WorkflowRepository;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.forums.BaseForumTag;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateAppliedTagsEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.*;
import services.GitHubService;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static bot.helpers.DevopsBotHelper.*;

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
    private Guild glacies;

    private final Set<Long> blockGithubMessage;

    @Value("${guild.id}")
    private String guildId;

    @Value("${github.token}")
    private String githubToken;

    @Value("${general.id}")
    private long generalId;

    @DiscordMapping
    private void ready(ReadyEvent event){
        glacies = event.getJDA().getGuildById(guildId);
    }

    @Autowired
    public DevopsBot(GithubRepository gitHubRepositories, WorkflowRepository workflowRepository, GithubIssueRepository githubIssueRepository, GithubUserRepository githubUserRepository, GitHubService gitHubService){
        this.workflowRepository = workflowRepository;
        this.gitHubRepositories = gitHubRepositories;
        this.githubIssueRepository = githubIssueRepository;
        this.githubUserRepository = githubUserRepository;
        this.gitHubService = gitHubService;
        mapper = new ObjectMapper();
        blockGithubMessage = new HashSet<>();
    }

    @DiscordMapping
    private void userMessage(MessageReceivedEvent event){
        if(!event.isFromThread()) return;
        long forumChannelId = event.getChannel().asThreadChannel().getParentChannel().asForumChannel().getIdLong();
        long threadChannelId = event.getChannel().getIdLong();
        githubIssueRepository.getGithubIssueByForumPostId(threadChannelId).ifPresent(githubIssue ->
                gitHubRepositories.getByForumChannelId(forumChannelId).ifPresent(gitRepo -> {
                    String message = event.getMessage().getContentDisplay();
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

    @DiscordMapping(Id = "spring", SubId = "properties", FocusedOption = "file")
    private void propertiesFilesAutoComplete(CommandAutoCompleteInteractionEvent event){
        List<Command.Choice> files = gitHubService.getPropertiesFileList().stream()
                .filter(file -> file.getPath().contains(event.getFocusedOption().getValue()))
                .map(file -> {
                    String[] paths = file.getPath().split("/");
                    return new Command.Choice(paths[paths.length - 1], file.getPath());
                })
                .toList();
        event.replyChoices(files).queue();
    }

    @DiscordMapping(Id = "spring", SubId = "properties")
    private void springProperties(
            SlashCommandInteractionEvent event,
            @EventProperty String file
    ){
        String content = gitHubService.getPropertiesFileContent(file);
        event.reply("```\n" + content + "\n```").setEphemeral(true).queue();
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
        HashMap<Class, Object> paramMap = new HashMap<>();
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
                        .addOption(OptionType.STRING, "file", "File to get the properties of", true, true)
            ),
            Commands.slash("github", "Github related commands").addSubcommands(
                new SubcommandData("add_token", "Add a github token to make comments under your user on issues you comment on in discord.")
                        .addOption(OptionType.STRING, "token", "Github token with repo access", true)
            )
        );
    }
}