package bot.listener;

import application.App;
import bot.utils.EmbedMessageGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zgamelogic.jda.AdvancedListenerAdapter;
import data.serializable.MonitoringConfig;
import interfaces.monitor.MonitorsInterfacer;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;

import static com.zgamelogic.jda.Annotations.*;

@RestController
@Slf4j
public class MonitoringBot extends AdvancedListenerAdapter {

    private TextChannel channel;
    private Message message;

    @OnReady
    public void ready(ReadyEvent event) {
        channel = event.getJDA().getGuildById(App.config.getGuildId()).getTextChannelById(App.config.getMonitoringId());
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
                message = channel.editMessageEmbedsById(messageId, EmbedMessageGenerator.monitorStatus(MonitorsInterfacer.getMonitorStatus())).complete();
            } catch (IOException ignored) {
                message = channel.sendMessageEmbeds(EmbedMessageGenerator.monitorStatus(MonitorsInterfacer.getMonitorStatus())).complete();
                try {
                    monitorConfigFile.getParentFile().mkdirs();
                    om.writeValue(monitorConfigFile, new MonitoringConfig(message.getId()));
                } catch (IOException e) {
                    log.error("error when writing to monitoring config", e);
                }
            }
        } else {
            message = channel.editMessageEmbedsById(message.getId(), EmbedMessageGenerator.monitorStatus(MonitorsInterfacer.getMonitorStatus())).complete();
        }
    }
}
