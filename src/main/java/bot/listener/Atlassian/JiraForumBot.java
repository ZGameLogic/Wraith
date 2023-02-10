package bot.listener.Atlassian;

import com.zgamelogic.AdvancedListenerAdapter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import static net.dv8tion.jda.api.entities.channel.ChannelType.GUILD_PUBLIC_THREAD;

public class JiraForumBot extends AdvancedListenerAdapter {

    private ForumChannel forum;
    private Guild guild;

    @Override
    public void onReady(ReadyEvent event) {
        forum = event.getJDA().getForumChannelById(1019776791939919882l);
        guild = event.getJDA().getGuildById(738850921706029168l);
        addToPost(1073419052401295400l, "This is me adding to post");
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if(!event.isFromGuild()) return;
        if(event.getAuthor().isBot()) return;
        if(event.getChannelType() != GUILD_PUBLIC_THREAD) return;
    }

    public void createPost(String title, String content){
        forum.createForumPost(title, MessageCreateData.fromContent(content)).queue();
    }

    public void addToPost(long id, String message){
        guild.getThreadChannelById(id).sendMessage(message).queue();
    }
}
