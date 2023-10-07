package bot.listener;

import com.zgamelogic.jda.AdvancedListenerAdapter;
import data.database.github.GithubRepository;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

@Slf4j
@RestController
public class DevopsBot extends AdvancedListenerAdapter {

    private final GithubRepository githubRepository;
    private final static File DATA_DIR = new File("jsons");

    @Autowired
    public DevopsBot(GithubRepository githubRepository){
        this.githubRepository = githubRepository;
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
    ) throws FileNotFoundException {
        JSONObject jsonBody = new JSONObject(body);
        File actionDir = new File(DATA_DIR.getPath() + "/" + githubEvent);
        int suffix = actionDir.listFiles() == null ? 0 : actionDir.listFiles().length;
        File json = new File(actionDir.getPath() + "/" + githubEvent + "-" + suffix + ".json");
        if(!actionDir.exists()) actionDir.mkdirs();
        PrintWriter out = new PrintWriter(json);
        out.println(jsonBody.toString(4));
        out.flush();
        out.close();
    }
}