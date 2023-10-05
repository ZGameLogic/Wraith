package bot.listener;

import com.zgamelogic.jda.AdvancedListenerAdapter;
import data.database.github.GithubRepository;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class DevopsBot extends AdvancedListenerAdapter {

    private final GithubRepository githubRepository;

    @Autowired
    public DevopsBot(GithubRepository githubRepository){
        this.githubRepository = githubRepository;
    }

    @GetMapping("health")
    private String healthCheck(){
        return "Healthy";
    }

    @PostMapping("github")
    private void github(@RequestBody String body){
        JSONObject jsonBody = new JSONObject(body);
        switch(jsonBody.getString("action")){
            case "created":
                break;
            case "deleted":
                break;
        }
    }
}