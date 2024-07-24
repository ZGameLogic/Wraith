package com.zgamelogic.discord.listeners;

import com.zgamelogic.annotations.DiscordController;
import com.zgamelogic.annotations.DiscordMapping;
import com.zgamelogic.annotations.EventProperty;
import com.zgamelogic.data.api.zGameLogic.SOTData;
import com.zgamelogic.data.discord.SeaOfThievesEventData;
import com.zgamelogic.discord.utils.EmbedMessageGenerator;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.springframework.context.annotation.Bean;
import com.zgamelogic.services.ZGameLogicService;

import java.util.List;

@DiscordController
@Slf4j
public class SeaOfThievesBot {

    private final ZGameLogicService zGameLogicService;

    public SeaOfThievesBot(ZGameLogicService zGameLogicService) {
        this.zGameLogicService = zGameLogicService;
    }

    @DiscordMapping(Id = "sot", SubId = "data-point")
    private void addData(
            SlashCommandInteractionEvent event,
            @EventProperty SeaOfThievesEventData data
    ){
        log.info("Event received");
        event.deferReply().queue();
        log.info("Event deferred");
        SOTData returnData = zGameLogicService.postSeoOfThievesData(data);
        log.info("Post request finished");
        event.getHook().sendMessageEmbeds(EmbedMessageGenerator.sotDataMessage(returnData)).queue();
        log.info("Event responded to");
    }

    @Bean
    private List<CommandData> slashCommands(){
        return List.of(
                Commands.slash("sot", "Slash Commands for sea of thieves").addSubcommands(
                        new SubcommandData("data-point", "Record a datapoint to the database")
                                .addOption(OptionType.BOOLEAN, "ben", "Did Ben join", true)
                                .addOption(OptionType.BOOLEAN, "greg", "Did Greg join", true)
                                .addOption(OptionType.BOOLEAN, "jj", "Did JJ join", true)
                                .addOption(OptionType.BOOLEAN, "patrick", "Did Patrick join", true)
                                .addOption(OptionType.BOOLEAN, "success", "Did this event happen", true)
                                .addOption(OptionType.STRING, "time", "What time did this take place", true)
                )
        );
    }
}
