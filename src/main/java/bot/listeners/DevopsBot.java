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
import net.dv8tion.jda.api.events.session.ReadyEvent;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
        gitHubService.getIssueLabels("https://api.github.com/repos/ZGameLogic/Discord-Bot/labels").forEach(label -> {
            System.out.println(label.getName());
        });
        List<String> tags = new LinkedList<>(List.of("bug", "documentation"));
        gitHubService.editIssueLabels("", 1, new LabelsPayload(tags));
    }

    @DiscordMapping
    private void forumTagUpdate(ChannelUpdateAppliedTagsEvent event){
//        long forumChannelId = event.getChannel().asThreadChannel().getParentChannel().asForumChannel().getIdLong();
//        long threadChannelId = event.getChannel().getIdLong();
//        githubIssueRepository.getGithubIssueByForumPostId(threadChannelId).ifPresent(githubIssue ->
//                gitHubRepositories.getByForumChannelId(forumChannelId).ifPresent(gitRepo -> {
//                    String[] tags = (String[]) event.getNewTags().stream().map(BaseForumTag::getName).toList().toArray();
//                    gitHubService.editIssueLabels(gitRepo.getRepoName(), githubIssue.getNumber(), tags);
//        }));
        List<String> tags = event.getNewTags().stream().map(BaseForumTag::getName).toList();
        gitHubService.editIssueLabels("", 1, new LabelsPayload(tags));
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
}