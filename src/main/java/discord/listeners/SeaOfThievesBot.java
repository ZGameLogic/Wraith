package discord.listeners;

import com.zgamelogic.annotations.DiscordController;
import com.zgamelogic.annotations.DiscordMapping;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.context.annotation.Bean;

@DiscordController
public class SeaOfThievesBot {

    @DiscordMapping(Id = "sea-of-thieves")
    private void addData(
            SlashCommandInteractionEvent event
    ){

    }

    @Bean
    private CommandData slashCommands(){
        return Commands.slash("sea-of-thieves", "Slash Commands for sea of thieves")
                .addOption(OptionType.BOOLEAN, "ben", "Did Ben join", true)
                .addOption(OptionType.BOOLEAN, "greg", "Did Greg join", true)
                .addOption(OptionType.BOOLEAN, "jj", "Did JJ join", true)
                .addOption(OptionType.BOOLEAN, "patrick", "Did Patrick join", true)
                .addOption(OptionType.BOOLEAN, "success", "Did this event happen", true)
                .addOption(OptionType.STRING, "time", "What time did this take place", true);
    }
}