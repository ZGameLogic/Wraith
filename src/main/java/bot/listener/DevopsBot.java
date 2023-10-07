package bot.listener;

import application.App;
import com.zgamelogic.jda.AdvancedListenerAdapter;
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

    private final GithubRepository githubRepositories;
    private final static File DATA_DIR = new File("jsons");
    private Guild glacies;

    @OnReady
    private void ready(ReadyEvent event){
        glacies = event.getJDA().getGuildById(App.config.getGuildId());
    }

    @Autowired
    public DevopsBot(GithubRepository githubRepositories){
        this.githubRepositories = githubRepositories;
        if(!DATA_DIR.exists()) {
            DATA_DIR.mkdirs();
        }
    }

    @GetMapping("health")
    private String healthCheck(){
        return "Healthy";
    }

    @PostMapping("github")
    private void github(
            @RequestHeader(name = "X-GitHub-Event") String githubEvent,
            @RequestBody String body
    ) {
        for(Method method: getClass().getDeclaredMethods()){
            if(method.isAnnotationPresent(GithubEvent.class)){
                GithubEvent annotation = method.getAnnotation(GithubEvent.class);
                try {
                    if(!annotation.value().equals(githubEvent)) continue;
                    if (!annotation.action().isEmpty() && !annotation.action().equals(
                            new JSONObject(body).getString("action")
                    )) continue;
                    method.invoke(this, body);
                } catch (IllegalAccessException | InvocationTargetException | JSONException e) {
                    log.error("Unable to run github method", e);
                }

            }
        }
    }

    @GithubEvent("push")
    private void githubPush(String body){}

    @GithubEvent(value = "repository", action = "created")
    private void githubRepositoryCreated(String body){}

    @GithubEvent(value = "repository", action = "deleted")
    private void githubRepositoryDeleted(String body){}

    @GithubEvent(value = "workflow_job", action = "queued")
    private void githubWorkflowQueued(String body){}

    @GithubEvent(value = "workflow_job", action = "completed")
    private void githubWorkflowCompleted(String body){}

    @GithubEvent(value = "workflow_job", action = "in_progress")
    private void githubWorkflowInProgress(String body){}

    @GithubEvent(value = "pull_request", action = "opened")
    private void githubPullRequestOpened(String body){}

    @GithubEvent(value = "pull_request", action = "ready_for_review")
    private void githubPullRequestReadyForReview(String body){}

    @GithubEvent(value = "pull_request", action = "closed")
    private void githubPullRequestClosed(String body){}

    private void createDiscordRepository(long id, String repoUrl, String repoName){
        repoName = repoName.replaceAll("-", " ");
        GitRepo repo = new GitRepo(id, repoUrl, repoName);
        // cat
        Category cat = glacies.createCategory(repoName).complete();
        // general
        TextChannel general = cat.createTextChannel(repoName + " general").complete();
        general.getManager().setTopic("General channel for repository: " + repoName + ".").queue();
        // pull request
        TextChannel prChannel = cat.createTextChannel(repoName + " general").complete();
        prChannel.getManager().setTopic("General channel for repository: " + repoName + ".").queue();
        // forum
        ForumChannel forumChannel = cat.createForumChannel(repoName + "-issues").complete();

        repo.setCategoryId(cat.getIdLong());
        repo.setGeneralId(general.getIdLong());
        repo.setPullRequestId(prChannel.getIdLong());
        repo.setForumChannelId(forumChannel.getIdLong());

        githubRepositories.save(repo);
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    private @interface GithubEvent {
        String value();
        String action() default "";
    }
}