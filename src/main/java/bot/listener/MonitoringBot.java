package bot.listener;

import bot.utils.EmbedMessageGenerator;
import com.zgamelogic.jda.AdvancedListenerAdapter;
import data.ConfigLoader;
import interfaces.monitor.MonitorsInterfacer;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import static com.zgamelogic.jda.Annotations.*;

@Controller
public class MonitoringBot extends AdvancedListenerAdapter {

    private TextChannel channel;
    private Message message;
    private final ConfigLoader config;

    @Autowired
    public MonitoringBot(ConfigLoader config){
        this.config = config;
    }

    @OnReady
    public void ready(ReadyEvent event) {
        channel = event.getJDA().getGuildById(config.getGuildId()).getTextChannelById(config.getMonitoringId());
    }

    public void update(){
        if(message == null){
            message = channel.sendMessageEmbeds(EmbedMessageGenerator.monitorStatus(MonitorsInterfacer.getMonitorStatus())).complete();
        } else {
            message = channel.editMessageEmbedsById(message.getId(), EmbedMessageGenerator.monitorStatus(MonitorsInterfacer.getMonitorStatus())).complete();
        }
    }
}
