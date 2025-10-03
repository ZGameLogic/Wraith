package com.zgamelogic.modrinth;

import com.zgamelogic.discord.annotations.DiscordController;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.springframework.context.annotation.Bean;

import java.util.List;

@DiscordController
@AllArgsConstructor
public class ModrinthBot {


    @Bean
    public List<CommandData> modrinthCommands(){
        return List.of(
            Commands.slash("modrinth", "Modrinth API listening").addSubcommands(
                new SubcommandData("follow", "Follow a modrinth project to get updates in this channel")
                    .addOption(OptionType.STRING, "project-id", "The id of the project", true),
                new SubcommandData("unfollow", "Unfollow a modrinth project for this channel")
                    .addOption(OptionType.STRING, "project", "The project to stop listening to", true),
                new SubcommandData("list", "List the followed projects in this channel")
            )
        );
    }
}

