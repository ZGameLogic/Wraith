package bot.listener;

import com.zgamelogic.jda.AdvancedListenerAdapter;
import data.database.atlassian.jira.projects.ProjectRepository;
import org.json.JSONObject;

public class BambooBot extends AdvancedListenerAdapter {

    private final ProjectRepository projectRepository;

    public BambooBot(ProjectRepository projectRepository){
        this.projectRepository = projectRepository;
    }

    public void handleBambooWebhook(JSONObject body){
        System.out.println(body);
    }
}
