package bot.listeners;

import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.SecretProperties;
import com.zgamelogic.annotations.DiscordController;
import com.zgamelogic.annotations.DiscordMapping;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import java.util.LinkedList;
import java.util.List;

@DiscordController
public class AzureBot {

    private final SecretClient secretClient;

    @Autowired
    public AzureBot(SecretClient secretClient) {
        this.secretClient = secretClient;
    }

    @DiscordMapping(Id = "azure", SubId = "secret", FocusedOption = "name")
    public void azureAutoFill(CommandAutoCompleteInteractionEvent event){
        List<Command.Choice> names = secretClient
                .listPropertiesOfSecrets()
                .stream()
                .map(SecretProperties::getName)
                .filter(word -> word.startsWith(event.getFocusedOption().getValue()))
                .map(word -> new Command.Choice(word, word))
                .toList();
        event.replyChoices(names).queue();
    }

    @DiscordMapping(Id = "azure", SubId = "secret")
    public void azureSecretSlashCommand(SlashCommandInteractionEvent event){
        String value = secretClient.getSecret(event.getOption("name").getAsString()).getValue();
        event.reply(value).setEphemeral(true).queue();
    }

    @DiscordMapping(Id = "azure", SubId = "add_secret")
    public void azureSecretAddSlashCommand(SlashCommandInteractionEvent event) {
        secretClient.setSecret(event.getOption("name").getAsString(), event.getOption("value").getAsString());
        event.reply("Secret created").setEphemeral(true).queue();
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
