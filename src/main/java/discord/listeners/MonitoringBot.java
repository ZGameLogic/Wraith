package discord.listeners;

import discord.utils.EmbedMessageGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zgamelogic.annotations.DiscordController;
import com.zgamelogic.annotations.DiscordMapping;
import data.serializable.MonitoringConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import services.DataOtterService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;

@RestController
@DiscordController
@Slf4j
public class MonitoringBot {

    private final DataOtterService dataOtterService;

    private TextChannel channel;
    private Message message;
    private final Environment environment;

    @Autowired
    public MonitoringBot(DataOtterService dataOtterService, Environment environment) {
        this.dataOtterService = dataOtterService;
        this.environment = environment;
    }

    @DiscordMapping
    public void ready(ReadyEvent event) {
        channel = event.getJDA().getGuildById(environment.getProperty("guild.id")).getTextChannelById(environment.getProperty("monitoring.id"));
    }

    @Scheduled(cron = "0 */1 * * * *")
    private void oneMinuteTask(){
        update();
    }

    public void update(){
        if(message == null){
            File monitorConfigFile = new File("data/monitoring/config.json");
            ObjectMapper om = new ObjectMapper();
            try {
                MonitoringConfig config = om.readValue(monitorConfigFile, MonitoringConfig.class);
                String messageId = config.getMessageId();
                message = channel.editMessageEmbedsById(messageId, EmbedMessageGenerator.monitorStatus(dataOtterService.getMonitorStatus())).complete();
            } catch (IOException ignored) {
                message = channel.sendMessageEmbeds(EmbedMessageGenerator.monitorStatus(dataOtterService.getMonitorStatus())).complete();
                try {
                    monitorConfigFile.getParentFile().mkdirs();
                    om.writeValue(monitorConfigFile, new MonitoringConfig(message.getId()));
                } catch (IOException e) {
                    log.error("error when writing to monitoring config", e);
                }
            }
        } else {
            message = channel.editMessageEmbedsById(message.getId(), EmbedMessageGenerator.monitorStatus(dataOtterService.getMonitorStatus())).complete();
        }
    }
}
