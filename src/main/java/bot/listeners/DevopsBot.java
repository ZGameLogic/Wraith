package bot.listeners;

import application.App;
import bot.helpers.DevopsBotHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zgamelogic.jda.AdvancedListenerAdapter;
import data.api.github.Repository;
import data.database.github.GithubRepository;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.zgamelogic.jda.Annotations.*;
import static bot.helpers.DevopsBotHelper.*;

@Slf4j
@RestController
public class DevopsBot extends AdvancedListenerAdapter {

    private final GithubRepository gitHubRepositories;
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
                    //
                    if(method.isAnnotationPresent(CreateDiscordRepo.class)){
                        JSONObject jsonRepo = new JSONObject(body).getJSONObject("repository");
                        Repository repo = mapper.readValue(jsonRepo.toString(), Repository.class);
                        if(!gitHubRepositories.existsById(repo.getId())) createDiscordRepository(repo, true, gitHubRepositories, glacies);
                    }
                    Class<?> parameterType = method.getParameterTypes()[0];
                    method.setAccessible(true);
                    if(parameterType == String.class){
                        method.invoke(DevopsBotHelper.class, body, gitHubRepositories, glacies);
                    } else {
                        method.invoke(DevopsBotHelper.class, mapper.readValue(body, parameterType), gitHubRepositories, glacies);
                    }
                } catch (IllegalAccessException | InvocationTargetException | JSONException | JsonProcessingException e) {
                    log.error("Unable to run gitHub method", e);
                }

            }
        }
    }
}