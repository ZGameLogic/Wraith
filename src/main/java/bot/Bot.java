package bot;

import application.App;
import bot.listener.*;
import data.ConfigLoader;
import data.database.atlassian.jira.issues.IssueRepository;
import data.database.atlassian.jira.projects.ProjectRepository;
import data.database.curseforge.CurseforgeRepository;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;

@Slf4j
@RestController
public class Bot {

    @Autowired
    private CurseforgeRepository curseforgeRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private IssueRepository issueRepository;

    private CurseForgeBot curseForgeBot;
    private JiraBot jiraBot;
    private BitbucketBot bitbucketBot;
    private BambooBot bambooBot;
    private DatadogBot datadogBot;

    private JDA bot;

    @PostConstruct
    public void start() {
        ConfigLoader config = App.config;

        // Create bot
        JDABuilder bot = JDABuilder.createDefault(config.getBotToken());
        bot.enableIntents(GatewayIntent.MESSAGE_CONTENT);
        bot.setEventPassthrough(true);

        curseForgeBot = new CurseForgeBot(curseforgeRepository);
        jiraBot = new JiraBot(projectRepository, issueRepository);
        bitbucketBot = new BitbucketBot(projectRepository);
        bambooBot = new BambooBot(projectRepository);
        datadogBot = new DatadogBot();

        bot.addEventListeners(curseForgeBot, new DevopsBot(), jiraBot, bitbucketBot, datadogBot);

        this.bot = bot.build();

        // Login
        try {
            this.bot.awaitReady();
        } catch (InterruptedException e) {
            log.error("Unable to launch bot");
        }

        tenMinuteTask();
    }

    @PostMapping("webhooks/jira")
    private void jiraWebhook(@RequestBody String body) throws JSONException {
        JSONObject jsonBody = new JSONObject(body);
        jiraBot.handleJiraWebhook(jsonBody);
    }

    @PostMapping("webhooks/bitbucket")
    private void bitbucketWebhook(@RequestBody String body) throws JSONException {
        JSONObject jsonBody = new JSONObject(body);
        if(jsonBody.has("eventKey")) bitbucketBot.handleBitbucketWebhook(jsonBody);
    }

    @PostMapping("webhooks/bamboo")
    private void bambooWebhook(@RequestBody String body) throws JSONException {
        JSONObject jsonBody = new JSONObject(body);
        bambooBot.handleBambooWebhook(jsonBody);
    }

    @PostMapping("webhooks/datadog")
    private void datadogWebhook(@RequestBody String body){
        log.info("Post " + body);
    }

    @GetMapping("webhooks/datadog")
    private void datadoggetWebhook(@RequestBody String body){
        log.info("Get " + body);
    }

    @GetMapping("health")
    private String healthCheck(){
        return "Healthy";
    }

    @Scheduled(cron = "0 */5 * * * *")
    private void fiveMinuteTask() {
        curseForgeBot.update();
    }

    @Scheduled(cron = "0 */30 * * * *")
    private void tenMinuteTask(){
        datadogBot.update();
    }
}
