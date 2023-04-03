package bot;

import application.App;
import bot.listener.*;
import data.ConfigLoader;
import data.database.atlassian.jira.issues.IssueRepository;
import data.database.atlassian.jira.projects.BitbucketProject;
import data.database.atlassian.jira.projects.Project;
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

    private JDA bot;

    @PostConstruct
    public void start(){
        ConfigLoader config = App.config;

        // Create bot
        JDABuilder bot = JDABuilder.createDefault(config.getBotToken());
        bot.enableIntents(GatewayIntent.MESSAGE_CONTENT);
        bot.setEventPassthrough(true);

        curseForgeBot = new CurseForgeBot(curseforgeRepository);
        jiraBot = new JiraBot(projectRepository, issueRepository);
        bitbucketBot = new BitbucketBot(projectRepository);
        bambooBot = new BambooBot(projectRepository);

        bot.addEventListeners(curseForgeBot, new DevopsBot(), jiraBot, bitbucketBot);

        this.bot = bot.build();

        // Login
        try {
            this.bot.awaitReady();
        } catch (InterruptedException e) {
            log.error("Unable to launch bot");
        }
//        Project test = new Project();
//        test.setProjectId(15);
//        test.setProjectKey("BSPR");
//        test.setProjectName("Test Project 2");
//        test.setCategoryId(2L);
//        test.setJiraChannelId(2L);
//        test.setForumChannelId(2L);
//        BitbucketProject project1 = new BitbucketProject();
//        project1.setChannelId(3L);
//        project1.setPullRequestChannelId(3L);
//        project1.setRepositoryId(3L);
//        project1.setProjectSlug("BSPR");
//        project1.setRepoSlug("test-repo-3");
//        BitbucketProject project2 = new BitbucketProject();
//        project2.setChannelId(4L);
//        project2.setPullRequestChannelId(4L);
//        project2.setRepositoryId(4L);
//        project2.setProjectSlug("BSPR");
//        project2.setRepoSlug("test-repo-4");
//        test.getBitbucketProjects().add(project1);
//        test.getBitbucketProjects().add(project2);
//        projectRepository.save(test);
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

    @GetMapping("health")
    private String healthCheck(){
        return "Healthy";
    }

    @Scheduled(cron = "0 */5 * * * *")
    private void fiveMinuteTask() {
        curseForgeBot.update();
    }
}
