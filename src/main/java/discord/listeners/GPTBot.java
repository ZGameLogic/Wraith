package discord.listeners;

import com.zgamelogic.annotations.DiscordController;
import jakarta.annotation.PostConstruct;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.springframework.context.annotation.Bean;
import services.GPTService;

@DiscordController
public class GPTBot {

    private final GPTService gptService;

    public GPTBot(GPTService gptService) {
        this.gptService = gptService;
    }

    @PostConstruct
    private void post(){
        gptService.generateImage("Baseball winning a trophy");
    }

    @Bean
    private SlashCommandData generateImageSlashCommand(){
        return Commands.slash("image", "Generates image with a prompt.")
                .addOption(OptionType.STRING, "prompt", "Prompt to generate the image with.", true);
    }
}
