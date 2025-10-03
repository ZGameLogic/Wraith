package com.zgamelogic.secrets;

import com.zgamelogic.secrets.database.Secret;
import com.zgamelogic.discord.annotations.DiscordController;
import com.zgamelogic.discord.annotations.DiscordExceptionHandler;
import com.zgamelogic.discord.annotations.DiscordMapping;
import com.zgamelogic.discord.annotations.EventProperty;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@DiscordController
@AllArgsConstructor
public class SecretsBot {
    private final SecretsService secretsService;

    @DiscordMapping(Id = "secrets", SubId = "set")
    public void setSecret(
        SlashCommandInteractionEvent event,
        @EventProperty String name,
        @EventProperty String value,
        @EventProperty IMentionable access
    ){
        long accessId = access != null ? access.getIdLong() : event.getMember().getIdLong();
        secretsService.setSecretValue(name, value, accessId);
        event.reply("Secret has been set").setEphemeral(true).queue();
    }

    @DiscordMapping(Id = "secrets", SubId = "get", FocusedOption = "name")
    public void getValidSecrets(CommandAutoCompleteInteractionEvent event, @EventProperty String name){
        List<Long> access = new ArrayList<>();
        access.add(event.getMember().getIdLong());
        access.addAll(event.getMember().getRoles().stream().map(ISnowflake::getIdLong).toList());
        List<Secret> secrets = secretsService.listAvailableSecrets(access);
        event.replyChoices(secrets.stream()
            .filter(s -> name == null || name.isEmpty() || s.getName().toLowerCase().contains(name.toLowerCase()))
            .sorted(Comparator.comparing(s -> s.getName().toLowerCase()))
            .map(secret -> new Command.Choice(secret.getName(), secret.getId().toString()))
            .limit(25)
            .toList()
        ).queue();
    }

    @DiscordMapping(Id = "secrets", SubId = "get")
    public void getSecret(SlashCommandInteractionEvent event, @EventProperty String name){
        List<Long> access = new ArrayList<>();
        access.add(event.getMember().getIdLong());
        access.addAll(event.getMember().getRoles().stream().map(ISnowflake::getIdLong).toList());
        Secret secret = secretsService.getSecretValue(UUID.fromString(name), access)
            .orElseThrow(() -> new RuntimeException("Secret not found"));
        event.reply("```" + secret.getValue() + "```").setEphemeral(true).queue();
    }

    @DiscordExceptionHandler({IllegalArgumentException.class, RuntimeException.class})
    public void exception(SlashCommandInteractionEvent event){
        event.reply("Unable to get secret.").setEphemeral(true).queue();
    }

    @Bean
    public SlashCommandData secretsSlashCommand(){
        return Commands.slash("secrets", "All the secrets commands").addSubcommands(
            new SubcommandData("get", "Get a secrets value")
                .addOption(OptionType.STRING,"name", "The name of the secret", true, true),
            new SubcommandData("set", "Set a secret value")
                .addOption(OptionType.STRING, "name", "The name of the secret", true)
                .addOption(OptionType.STRING, "value", "The secret value", true)
                .addOption(OptionType.MENTIONABLE, "access", "Allow specific access to the secret")
        );
    }
}

