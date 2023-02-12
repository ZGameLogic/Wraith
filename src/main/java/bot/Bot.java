package bot;

import application.App;
import bot.listener.Atlassian.JiraForumBot;
import bot.listener.CurseForgeBot;
import bot.listener.atlassian.BambooBot;
import bot.listener.atlassian.BitbucketBot;
import bot.listener.atlassian.JiraBot;
import data.ConfigLoader;
import data.database.atlassian.AtlassianRepository;
import data.database.curseforge.CurseforgeRepository;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;

@Slf4j
@RestController
public class Bot {

    @Autowired
    private CurseforgeRepository curseforgeRepository;

    private CurseForgeBot curseForgeBot;
    private JiraForumBot jiraForumBot;
    @Autowired
    private AtlassianRepository atlassianRepository;

    private CurseForgeBot CFB;
    private JiraBot jiraBot;
    private BitbucketBot bitbucketBot;
    private BambooBot bambooBot;

    private JDA bot;

    @PostConstruct
    public void start(){
        ConfigLoader config = App.config;

        // Create bot
        JDABuilder bot = JDABuilder.createDefault(config.getBotToken());
        bot.enableIntents(GatewayIntent.MESSAGE_CONTENT);
        bot.setEventPassthrough(true);

        CFB = new CurseForgeBot(curseforgeRepository);

        bot.addEventListeners(CFB);

        this.bot = bot.build();

        // Login
        try {
            this.bot.awaitReady();
        } catch (InterruptedException e) {
            log.error("Unable to launch bot");
        }
    }

    @PostMapping("webhooks/jira")
    private void jiraWebhook(@RequestBody String body) throws JSONException {
        JSONObject jsonBody = new JSONObject(body);
        jiraBot.handleWebhook(jsonBody);
    }

    @PostMapping("webhooks/bitbucket")
    private void bitbucketWebhook(@RequestBody String body) throws JSONException {
        JSONObject jsonBody = new JSONObject(body);
        bitbucketBot.handleWebhook(jsonBody);
    }

    @PostMapping("webhooks/bamboo")
    private void bambooWebhook(@RequestBody String body) throws JSONException {
        JSONObject jsonBody = new JSONObject(body);
        bambooBot.handleWebhook(jsonBody);
    }

    @Scheduled(cron = "0 */5 * * * *")
    private void fiveMinuteTask() {
        curseForgeBot.update();
    }
}
