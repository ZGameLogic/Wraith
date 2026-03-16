package com.zgamelogic.dns;

import com.zgamelogic.discord.annotations.DiscordController;
import com.zgamelogic.discord.annotations.EventProperty;
import com.zgamelogic.discord.annotations.mappings.SlashCommandMapping;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.springframework.context.annotation.Bean;

@DiscordController
@AllArgsConstructor
public class DnsBot {
    private final Route53Service route53Service;

    @SlashCommandMapping(id = "cname")
    public void addCname(SlashCommandInteractionEvent event, @EventProperty String prefix){
        event.deferReply().setEphemeral(true).queue();
        route53Service.addCnameRecord(prefix);
        event.getHook().sendMessage("CNAME record created").setEphemeral(true).queue();
    }

    @Bean
    public SlashCommandData slashCommandData() {
        return Commands.slash("cname", "Create a cname record for zgamelogic.com")
            .addOption(OptionType.STRING, "prefix", "The prefix of the cname", true);
    }
}
