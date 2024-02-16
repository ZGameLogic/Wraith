package bot.listeners;

import com.zgamelogic.annotations.Bot;
import com.zgamelogic.annotations.DiscordController;
import com.zgamelogic.annotations.DiscordMapping;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

@DiscordController
public class GeneralListener {

    @Bot
    private JDA bot;

    @DiscordMapping
    public void onReady(ReadyEvent event){
        event.getJDA().updateCommands().addCommands(
                Commands.slash("test", "Bep")
        ).queue();
    }
}
