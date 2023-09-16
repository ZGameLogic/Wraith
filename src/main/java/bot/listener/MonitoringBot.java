package bot.listener;

import application.App;
import bot.utils.EmbedMessageGenerator;
import com.zgamelogic.jda.AdvancedListenerAdapter;
import interfaces.monitor.MonitorsInterfacer;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RestController;

import static com.zgamelogic.jda.Annotations.*;

@RestController
public class MonitoringBot extends AdvancedListenerAdapter {

    private TextChannel channel;
    private Message message;

    @OnReady
    public void ready(ReadyEvent event) {
        channel = event.getJDA().getGuildById(App.config.getGuildId()).getTextChannelById(App.config.getMonitoringId());
    }


    @Scheduled(cron = "0 */1 * * * *")
    private void tenMinuteTask(){
        update();
    }

    public void update(){
        if(message == null){
            message = channel.sendMessageEmbeds(EmbedMessageGenerator.monitorStatus(MonitorsInterfacer.getMonitorStatus())).complete();
        } else {
            message = channel.editMessageEmbedsById(message.getId(), EmbedMessageGenerator.monitorStatus(MonitorsInterfacer.getMonitorStatus())).complete();
        }
    }
}
