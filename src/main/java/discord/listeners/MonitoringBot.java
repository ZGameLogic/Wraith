package discord.listeners;

import data.database.discord.DiscordRepository;
import data.database.discord.ServerConfig;
import com.zgamelogic.annotations.DiscordController;
import com.zgamelogic.annotations.DiscordMapping;
import discord.utils.EmbedMessageGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import services.DataOtterService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RestController;

@RestController
@DiscordController
@Slf4j
public class MonitoringBot {

    private final DataOtterService dataOtterService;

    private TextChannel channel;
    private final Environment environment;

    @Value("${guild.id}")
    private long guildId;

    private final DiscordRepository discordRepository;

    @Autowired
    public MonitoringBot(DataOtterService dataOtterService, Environment environment, DiscordRepository discordRepository) {
        this.dataOtterService = dataOtterService;
        this.environment = environment;
        this.discordRepository = discordRepository;
    }

    @DiscordMapping
    public void ready(ReadyEvent event) {
        channel = event.getJDA().getGuildById(guildId).getTextChannelById(environment.getProperty("monitoring.id"));
    }

    @Scheduled(cron = "0 */1 * * * *")
    private void oneMinuteTask(){
        update();
    }

    public void update(){
        ServerConfig config = discordRepository.findById(guildId).orElseGet(() -> new ServerConfig(guildId));
        if(!config.hasMonitoringMessage()){
            Message message = channel.sendMessageEmbeds(EmbedMessageGenerator.monitorStatus(dataOtterService.getMonitorStatus())).complete();
            config.setMonitoringMessageId(message.getIdLong());
            discordRepository.save(config);
        } else {
            channel.editMessageEmbedsById(config.getMonitoringMessageId(), EmbedMessageGenerator.monitorStatus(dataOtterService.getMonitorStatus())).queue();
        }
    }
}
