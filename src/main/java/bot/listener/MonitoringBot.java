package bot.listener;

import application.App;
import bot.utils.EmbedMessageGenerator;
import com.zgamelogic.AdvancedListenerAdapter;
import interfaces.monitor.MonitorsInterfacer;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.session.ReadyEvent;

public class MonitoringBot extends AdvancedListenerAdapter {

    private TextChannel channel;
    private Message message;

    @Override
    public void onReady(ReadyEvent event) {
        channel = event.getJDA().getGuildById(App.config.getGuildId()).getTextChannelById(App.config.getMonitoringId());
    }

    public void update(){
        if(message == null){
            message = channel.sendMessageEmbeds(EmbedMessageGenerator.monitorStatus(MonitorsInterfacer.getMonitorStatus())).complete();
        } else {
            message = channel.editMessageEmbedsById(message.getId(), EmbedMessageGenerator.monitorStatus(MonitorsInterfacer.getMonitorStatus())).complete();
        }
    }
}
