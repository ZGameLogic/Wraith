package bot.listener.atlassian;

import com.zgamelogic.AdvancedListenerAdapter;
import data.database.atlassian.AtlassianConfig;
import data.database.atlassian.AtlassianRepository;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import org.json.JSONObject;

@Slf4j
public class JiraBot extends AdvancedListenerAdapter {

    private AtlassianRepository configs;

    public JiraBot(AtlassianRepository configs){
        this.configs = configs;
    }

    @Override
    public void onReady(ReadyEvent event) {
        new Thread(() -> {
            // Make sure the guild exists in the database
            for(Guild guild: event.getJDA().getGuilds()){
                if(configs.existsById(guild.getIdLong())) continue;
                log.info("Creating database entry for " + guild.getName());
                AtlassianConfig ac = new AtlassianConfig(guild.getIdLong());
                configs.save(ac);
            }
            // Make sure the guild is ready
            for(AtlassianConfig config: configs.findAll()){
                if(config.getThreadChannelId() == null) {
                    Guild guild = event.getJDA().getGuildById(config.getId());
                    log.info("Creating forum channel for " + guild.getName());
                    ForumChannel channel = guild.createForumChannel("Tickets").complete();
                    config.setThreadChannelId(channel.getIdLong());
                    configs.save(config);
                }
            }
        }, "On-Ready").start();
    }

    public void handleWebhook(JSONObject body){

    }


}
