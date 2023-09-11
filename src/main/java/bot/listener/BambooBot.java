package bot.listener;

import com.zgamelogic.jda.AdvancedListenerAdapter;
import data.database.atlassian.jira.projects.ProjectRepository;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BambooBot extends AdvancedListenerAdapter {

    private final ProjectRepository projectRepository;

    @Autowired
    public BambooBot(ProjectRepository projectRepository){
        this.projectRepository = projectRepository;
    }

    @PostMapping("webhooks/bamboo")
    private void bambooWebhook(@RequestBody String body) throws JSONException {
        JSONObject jsonBody = new JSONObject(body);
        handleBambooWebhook(jsonBody);
    }

    public void handleBambooWebhook(JSONObject body){
        System.out.println(body);
    }

}
