package bot;

import application.App;
import bot.listener.Atlassian.JiraForumBot;
import bot.listener.CurseForgeBot;
import data.ConfigLoader;
import data.database.curseforge.CurseforgeRepository;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;

@Slf4j
@RestController
public class Bot {

    @Autowired
    private CurseforgeRepository curseforgeRepository;

    private CurseForgeBot curseForgeBot;
    private JiraForumBot jiraForumBot;

    private JDA bot;

    @PostConstruct
    public void start(){
        ConfigLoader config = App.config;

        // Create bot
        JDABuilder bot = JDABuilder.createDefault(config.getBotToken());
        bot.enableIntents(GatewayIntent.MESSAGE_CONTENT);
        bot.setEventPassthrough(true);

        curseForgeBot = new CurseForgeBot(curseforgeRepository);
        jiraForumBot = new JiraForumBot();

        bot.addEventListeners(curseForgeBot, jiraForumBot);

        this.bot = bot.build();

        // Login
        try {
            this.bot.awaitReady();
        } catch (InterruptedException e) {
            log.error("Unable to launch bot");
        }
    }

    @Scheduled(cron = "0 */5 * * * *")
    private void fiveMinuteTask() {
        curseForgeBot.update();
    }
}
