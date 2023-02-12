package bot.listener.atlassian;

import com.zgamelogic.AdvancedListenerAdapter;
import data.database.atlassian.AtlassianConfig;
import data.database.atlassian.AtlassianRepository;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.json.JSONObject;

@Slf4j
public class JiraBot extends AdvancedListenerAdapter {

    private AtlassianRepository configs;
    private JDA bot;

    public JiraBot(AtlassianRepository configs){
        this.configs = configs;
    }

    @Override
    public void onReady(ReadyEvent event) {
        bot = event.getJDA();
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
                if(config.getForumChannelId() == null) {
                    Guild guild = event.getJDA().getGuildById(config.getId());
                    log.info("Creating forum channel for " + guild.getName());
                    ForumChannel channel = guild.createForumChannel("Tickets").complete();
                    config.setForumChannelId(channel.getIdLong());
                    configs.save(config);
                }
            }
        }, "On-Ready").start();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

    }

    public void handleWebhook(JSONObject body){
        System.out.println(body);
    }

    public void createPost(long guildId, String title, String content){
        AtlassianConfig ac = configs.getOne(guildId);
        ForumChannel forum = bot.getGuildById(guildId).getForumChannelById(ac.getForumChannelId());
        forum.createForumPost(title, MessageCreateData.fromContent(content)).queue();
    }

    public void addToPost(long guildId, String issue, String message){

    }
}
