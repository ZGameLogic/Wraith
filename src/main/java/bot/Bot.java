package bot;

import application.App;
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

    private CurseForgeBot CFB;

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

    @Scheduled(cron = "0 */5 * * * *")
    private void fiveMinuteTask() {
        CFB.update();
    }
}
