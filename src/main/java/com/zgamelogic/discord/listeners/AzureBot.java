package com.zgamelogic.discord.listeners;

import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.security.keyvault.secrets.models.SecretProperties;
import com.zgamelogic.annotations.DiscordController;
import com.zgamelogic.annotations.DiscordMapping;
import com.zgamelogic.annotations.EventProperty;
import com.zgamelogic.services.AzurePortalService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.*;

@DiscordController
@Slf4j
public class AzureBot {

    private final SecretClient secretClient;
    private final List<SecretProperties> azureSecrets;
    private final AzurePortalService azurePortalService;

    @Value("${admin.id}")
    private long adminId;

    @Autowired
    public AzureBot(SecretClient secretClient, AzurePortalService azurePortalService) {
        this.secretClient = secretClient;
        this.azurePortalService = azurePortalService;
        azureSecrets = new ArrayList<>();
        new Thread(this::fiveMinuteUpdate, "Azure pre-cache").start();
    }

    @DiscordMapping(Id = "azure", SubId = "secret", FocusedOption = "name")
    public void azureAutoFill(CommandAutoCompleteInteractionEvent event){
        List<Command.Choice> names = azureSecrets.stream().filter(secret -> {
                    if(secret.getTags() == null || !secret.getTags().containsKey("discord")) return false;
                    String tagValue = secret.getTags().get("discord");
                    if(tagValue.equals("public")) return true;
                    if(tagValue.equals("admin") && event.getMember().getRoles().stream().anyMatch(role -> role.getIdLong() == adminId)) return true;
                    return tagValue.equals(event.getUser().getId());
                })
                .map(SecretProperties::getName)
                .filter(word -> word.toLowerCase().startsWith(event.getFocusedOption().getValue().toLowerCase()))
                .sorted(Comparator.comparing(String::toLowerCase))
                .map(word -> new Command.Choice(word, word))
                .limit(25)
                .toList();
        try {
            event.replyChoices(names).queue();
        } catch (Exception e){
            log.error("Azure secrets took too long to get...", e);
        }
    }

    @DiscordMapping(Id = "azure", SubId = "secret")
    public void azureSecretSlashCommand(
            SlashCommandInteractionEvent event,
            @EventProperty String name
    ){
        azureSecrets.stream().filter(secret -> {
            if(secret.getTags() == null || !secret.getTags().containsKey("discord")) return false;
            String tagValue = secret.getTags().get("discord");
            if(tagValue.equals("public")) return true;
            if(tagValue.equals("admin") && event.getMember().getRoles().stream().anyMatch(role -> role.getIdLong() == adminId)) return true;
            return tagValue.equals(event.getUser().getId());
        }).filter(secret -> secret.getName().equals(name)).findFirst().ifPresentOrElse(
                secret -> event.reply("```" + secretClient.getSecret(secret.getName()).getValue() + "```").setEphemeral(true).queue(),
                () -> event.reply("You do not have permissions to see this secret").setEphemeral(true).queue()
        );
    }

    @DiscordMapping(Id = "azure", SubId = "add_cname")
    public void azureCnameCommand(
            SlashCommandInteractionEvent event,
            @EventProperty String name
    ){
        if(event.getMember().getIdLong() != 983846164162027570L){
            event.reply("You do not have permissions to do this").setEphemeral(true).queue();
            return;
        }
        try {
            azurePortalService.addCnameRecord(name);
            event.reply("CNAME record created").setEphemeral(true).queue();
        } catch (Exception e){
            log.error("Error creating cname record", e);
        }
    }

    @DiscordMapping(Id = "azure", SubId = "add_secret")
    public void azureSecretAddSlashCommand(
            SlashCommandInteractionEvent event,
            @EventProperty String name,
            @EventProperty String value
    ){
        KeyVaultSecret secret = new KeyVaultSecret(name, value);
        Map<String, String> tags = new HashMap<>();
        tags.put("username", event.getUser().getEffectiveName());
        tags.put("discord", event.getUser().getId());
        secret.setProperties(new SecretProperties().setTags(tags));
        try {
            secretClient.setSecret(secret);
            event.reply("Secret created").setEphemeral(true).queue();
            azureSecrets.add(secret.getProperties());
        } catch(Exception e){
            event.reply(e.getMessage()).setEphemeral(true).queue();
        }
    }

    @Scheduled(cron = "0 */5 * * * *")
    private void fiveMinuteUpdate(){
        azureSecrets.clear();
        azureSecrets.addAll(secretClient.listPropertiesOfSecrets().stream().toList());
    }

    @Bean
    private List<CommandData> azureSlashCommands(){
        return new LinkedList<>(List.of(
                Commands.slash("azure", "Commands dealing with Azure").addSubcommands(
                    new SubcommandData("secret", "Get a secret from the keyvault")
                        .addOption(OptionType.STRING, "name", "Secret name to get the value of", true, true),
                    new SubcommandData("add_secret", "Create a secret in the keyvault")
                        .addOption(OptionType.STRING, "name", "Name of the secret", true)
                        .addOption(OptionType.STRING, "value", "Value of the secret", true),
                    new SubcommandData("add_cname", "Create a cname record that points to zgamelogic.com")
                        .addOption(OptionType.STRING, "name", "Name of the cname record", true)
        )));
    }
}
