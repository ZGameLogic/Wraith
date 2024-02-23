package bot.listeners;

import bot.helpers.DevopsBotHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zgamelogic.annotations.DiscordController;
import com.zgamelogic.annotations.DiscordMapping;
import data.api.github.LabelsPayload;
import data.database.github.Issue.GithubIssueRepository;
import data.database.github.repository.GithubRepository;
import data.database.github.workflow.WorkflowRepository;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.forums.BaseForumTag;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateAppliedTagsEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static bot.helpers.DevopsBotHelper.*;

@Slf4j
@DiscordController
@RestController
public class DevopsBot {

    private final GithubRepository gitHubRepositories;
    private final WorkflowRepository workflowRepository;
    private final GithubIssueRepository githubIssueRepository;
    private final ObjectMapper mapper;
    private final GitHubService gitHubService;
    private Guild glacies;

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
    public DevopsBot(GithubRepository gitHubRepositories, WorkflowRepository workflowRepository, GithubIssueRepository githubIssueRepository, GitHubService gitHubService){
        this.workflowRepository = workflowRepository;
        this.gitHubRepositories = gitHubRepositories;
        this.githubIssueRepository = githubIssueRepository;
        this.gitHubService = gitHubService;
        mapper = new ObjectMapper();
    }

    @DiscordMapping(Id = "Spring", SubId = "properties", FocusedOption = "file")
    private void propertiesFilesAutoComplete(CommandAutoCompleteInteractionEvent event){
        // TODO implement
    }

    @DiscordMapping(Id = "spring", SubId = "properties")
    private void springProperties(SlashCommandInteractionEvent event){
        // TODO implement
    }

    @DiscordMapping
    private void forumTagUpdate(ChannelUpdateAppliedTagsEvent event){
        long forumChannelId = event.getChannel().asThreadChannel().getParentChannel().asForumChannel().getIdLong();
        long threadChannelId = event.getChannel().getIdLong();
        githubIssueRepository.getGithubIssueByForumPostId(threadChannelId).ifPresent(githubIssue ->
                gitHubRepositories.getByForumChannelId(forumChannelId).ifPresent(gitRepo -> {
                    List<String> tags = event.getNewTags().stream().map(BaseForumTag::getName).toList();
                    gitHubService.editIssueLabels(gitRepo.getRepoName(), githubIssue.getNumber(), new LabelsPayload(tags));
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
    private CommandData githubCommands(){
        return Commands.slash("spring", "Github commands").addSubcommands(
                new SubcommandData("properties", "Spring properties")
                        .addOption(OptionType.STRING, "file", "File to get the properties of", true, true)
        );
    }
}