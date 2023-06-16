package bot.listener;

import application.App;
import bot.utils.EmbedMessageGenerator;
import com.zgamelogic.AdvancedListenerAdapter;
import data.api.datadog.Monitor;
import interfaces.datadog.DatadogInterfacer;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.session.ReadyEvent;

public class DatadogBot extends AdvancedListenerAdapter {

    private TextChannel datadogUpdatesChannel;

    @Override
    public void onReady(ReadyEvent event) {
        datadogUpdatesChannel = event.getJDA().getGuildById(App.config.getGuildId()).getTextChannelById(App.config.getDatadogChannelId());
    }

    public void update(){
        Monitor[] monitors = DatadogInterfacer.getMonitors();
        datadogUpdatesChannel.sendMessageEmbeds(EmbedMessageGenerator.datadogMonitor(monitors)).queue();
    }
}
