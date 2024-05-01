package discord.listeners;

import com.zgamelogic.annotations.DiscordController;
import com.zgamelogic.annotations.DiscordMapping;
import com.zgamelogic.annotations.EventProperty;
import data.discord.SeaOfThievesEventData;
import data.discord.SeaOfThievesIslandData;
import discord.utils.EmbedMessageGenerator;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.utils.FileUpload;
import org.springframework.context.annotation.Bean;
import services.ImageProcessingService;
import services.ZGameLogicService;

import java.util.List;

@DiscordController
@Slf4j
public class SeaOfThievesBot {

    private final ZGameLogicService zGameLogicService;
    private final ImageProcessingService imageProcessingService;

    public SeaOfThievesBot(ZGameLogicService zGameLogicService, ImageProcessingService imageProcessingService) {
        this.zGameLogicService = zGameLogicService;
        this.imageProcessingService = imageProcessingService;
    }

    @DiscordMapping(Id = "sot", SubId = "data-point")
    private void addData(
            SlashCommandInteractionEvent event,
            @EventProperty SeaOfThievesEventData data
    ){
        boolean success = zGameLogicService.postSeoOfThievesData(data);
        event.reply("Data " + (success ? "recorded" : "not recorded")).setEphemeral(true).queue();
    }

    @DiscordMapping(Id = "sot", SubId = "island")
    private void sotIsland(
            SlashCommandInteractionEvent event,
            @EventProperty Message.Attachment image
    ){
        if(image == null || !image.getContentType().contains("image")){
            event.reply("This must be an image!").setEphemeral(true).queue();
            return;
        }
        event.deferReply().complete();
        image.getProxy().download().thenAccept(download -> {
            SeaOfThievesIslandData data = imageProcessingService.getSOTIslandFromPng(download);
            event.getHook().sendFiles(
                    FileUpload.fromData(data.islandPng())
            ).setEmbeds(
                    EmbedMessageGenerator.sotIslandDataMessage(data)
            ).queue();
        });
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
//                        ,
//                        new SubcommandData("island", "Find an island")
//                                .addOption(OptionType.ATTACHMENT, "image", "Image of the island", true)
                )
        );
    }
}
