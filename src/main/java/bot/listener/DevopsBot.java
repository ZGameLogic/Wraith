package bot.listener;

import application.App;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zgamelogic.jda.AdvancedListenerAdapter;
import data.api.github.Repository;
import data.database.github.GitRepo;
import data.database.github.GithubRepository;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.zgamelogic.jda.Annotations.*;

@Slf4j
@RestController
public class DevopsBot extends AdvancedListenerAdapter {

    private final GithubRepository gitHubRepositories;
    private final static File DATA_DIR = new File("jsons");
    private final ObjectMapper mapper;
    private Guild glacies;

    @OnReady
    private void ready(ReadyEvent event){
        glacies = event.getJDA().getGuildById(App.config.getGuildId());
    }

    @Autowired
    public DevopsBot(GithubRepository gitHubRepositories){
        mapper = new ObjectMapper();
        this.gitHubRepositories = gitHubRepositories;
        if(!DATA_DIR.exists()) {
            DATA_DIR.mkdirs();
        }
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
        for(Method method: getClass().getDeclaredMethods()){
            if(method.isAnnotationPresent(GithubEvent.class)){
                GithubEvent annotation = method.getAnnotation(GithubEvent.class);
                try {
                    // check if this is the right event
                    if(!annotation.value().equals(gitHubEvent)) continue;
                    // check again if this is the right more specific event
                    if (!annotation.action().isEmpty() && !annotation.action().equals(
                            new JSONObject(body).getString("action")
                    )) continue;
                    //
                    if(method.isAnnotationPresent(CreateDiscordRepo.class)){
                        JSONObject jsonRepo = new JSONObject(body).getJSONObject("repository");
                        Repository repo = mapper.readValue(jsonRepo.toString(), Repository.class);
                        if(!gitHubRepositories.existsById(repo.getId())) createDiscordRepository(repo);
                    }
                    method.invoke(this, body);
                } catch (IllegalAccessException | InvocationTargetException | JSONException | JsonProcessingException e) {
                    log.error("Unable to run gitHub method", e);
                }

            }
        }
    }

    @GithubEvent("push")
    private void gitHubPush(String body){}

    @CreateDiscordRepo
    @GithubEvent(value = "repository", action = "created")
    private void gitHubRepositoryCreated(String body){}

    @GithubEvent(value = "repository", action = "deleted")
    private void gitHubRepositoryDeleted(String body){
        deleteDiscordRepository(
                new JSONObject(body).getJSONObject("repository").getLong("id")
        );
    }

    @GithubEvent(value = "workflow_job", action = "queued")
    private void gitHubWorkflowQueued(String body){}

    @GithubEvent(value = "workflow_job", action = "completed")
    private void gitHubWorkflowCompleted(String body){}

    @GithubEvent(value = "workflow_job", action = "in_progress")
    private void gitHubWorkflowInProgress(String body){}

    @GithubEvent(value = "pull_request", action = "opened")
    private void gitHubPullRequestOpened(String body){}

    @GithubEvent(value = "pull_request", action = "ready_for_review")
    private void gitHubPullRequestReadyForReview(String body){}

    @GithubEvent(value = "pull_request", action = "closed")
    private void gitHubPullRequestClosed(String body){}

    /**
     * Update the glacies discord server by creating new channels for the git repository
     * @param repository GitHub repository to be updated
     */
    private void createDiscordRepository(Repository repository){
        long id = repository.getId();
        String repoName = repository.getName();
        String repoUrl = repository.getHtml_url();
        repoName = repoName.replaceAll("-", " ");
        GitRepo repo = new GitRepo(id, repoName, repoUrl);
        // cat
        Category cat = glacies.createCategory(repoName).complete();
        // general
        TextChannel general = cat.createTextChannel(repoName + " general").complete();
        general.getManager().setTopic("General channel for repository: " + repoName + ".").queue();
        // pull request
        TextChannel prChannel = cat.createTextChannel(repoName + " pull-requests").complete();
        prChannel.getManager().setTopic("Pull request channel for repository: " + repoName + ".").queue();
        // forum
        ForumChannel forumChannel = cat.createForumChannel(repoName + "-issues").complete();

        repo.setCategoryId(cat.getIdLong());
        repo.setGeneralId(general.getIdLong());
        repo.setPullRequestId(prChannel.getIdLong());
        repo.setForumChannelId(forumChannel.getIdLong());

        gitHubRepositories.save(repo);
    }

    /**
     * Update the glacies discord server by deleting channels for a git repository
     * @param id unique identifier of the GitHub repository
     */
    private void deleteDiscordRepository(long id){
        gitHubRepositories.findById(id).ifPresent(repo -> {
            glacies.getTextChannelById(repo.getPullRequestId()).delete().queue();
            glacies.getTextChannelById(repo.getGeneralId()).delete().queue();
            glacies.getForumChannelById(repo.getForumChannelId()).delete().queue();
            glacies.getCategoryById(repo.getCategoryId()).delete().queue();
            gitHubRepositories.delete(repo);
        });
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    private @interface GithubEvent {
        String value();
        String action() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    private @interface CreateDiscordRepo {}
}