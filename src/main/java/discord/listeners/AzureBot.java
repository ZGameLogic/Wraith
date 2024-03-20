package discord.listeners;

import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.security.keyvault.secrets.models.SecretProperties;
import com.zgamelogic.annotations.DiscordController;
import com.zgamelogic.annotations.DiscordMapping;
import com.zgamelogic.annotations.EventProperty;
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

import java.util.*;

@DiscordController
@Slf4j
public class AzureBot {

    private final SecretClient secretClient;

    @Value("${admin.id}")
    private long adminId;

    @Autowired
    public AzureBot(SecretClient secretClient) {
        this.secretClient = secretClient;
        new Thread(() -> secretClient.listPropertiesOfSecrets().stream().forEach(secret -> log.debug(secret.getName())), "Azure pre-cache").start();
    }

    @DiscordMapping(Id = "azure", SubId = "secret", FocusedOption = "name")
    public void azureAutoFill(CommandAutoCompleteInteractionEvent event){
        List<Command.Choice> names = secretClient
                .listPropertiesOfSecrets()
                .stream()
                .filter(secret -> {
                    if(secret.getTags() == null || !secret.getTags().containsKey("discord")) return false;
                    String tagValue = secret.getTags().get("discord");
                    if(tagValue.equals("public")) return true;
                    if(tagValue.equals("admin") && event.getMember().getRoles().stream().anyMatch(role -> role.getIdLong() == adminId)) return true;
                    return tagValue.equals(event.getUser().getId());
                })
                .map(SecretProperties::getName)
                .filter(word -> word.startsWith(event.getFocusedOption().getValue()))
                .map(word -> new Command.Choice(word, word))
                .toList();
        try {
            event.replyChoices(names).queue();
        } catch (Exception e){
            log.error("Azure secrets took too long to get...");
        }
    }

    @DiscordMapping(Id = "azure", SubId = "secret")
    public void azureSecretSlashCommand(
            SlashCommandInteractionEvent event,
            @EventProperty String name
    ){
        String value = secretClient.getSecret(name).getValue();
        event.reply("```" + value + "```").setEphemeral(true).queue();
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
        } catch(Exception e){
            event.reply(e.getMessage()).setEphemeral(true).queue();
        }
    }

    @Bean
    private List<CommandData> azureSlashCommands(){
        return new LinkedList<>(List.of(
                Commands.slash("azure", "Commands dealing with Azure").addSubcommands(
                    new SubcommandData("secret", "Get a secret from the keyvault")
                        .addOption(OptionType.STRING, "name", "Secret name to get the value of", true, true),
                    new SubcommandData("add_secret", "Create a secret in the keyvault")
                        .addOption(OptionType.STRING, "name", "Name of the secret", true)
                        .addOption(OptionType.STRING, "value", "Value of the secret", true)
        )));
    }
}
