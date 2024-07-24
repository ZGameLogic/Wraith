package com.zgamelogic.discord.listeners;

import com.zgamelogic.data.database.discord.DiscordRepository;
import com.zgamelogic.data.database.discord.ServerConfig;
import com.zgamelogic.annotations.DiscordController;
import com.zgamelogic.annotations.DiscordMapping;
import com.zgamelogic.discord.utils.EmbedMessageGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import com.zgamelogic.services.DataOtterMonitorsService;
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

    private final DataOtterMonitorsService dataOtterMonitorsService;

    private TextChannel channel;
    private final Environment environment;

    @Value("${guild.id}")
    private long guildId;

    private final DiscordRepository discordRepository;

    @Autowired
    public MonitoringBot(DataOtterMonitorsService dataOtterMonitorsService, Environment environment, DiscordRepository discordRepository) {
        this.dataOtterMonitorsService = dataOtterMonitorsService;
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
            Message message = channel.sendMessageEmbeds(EmbedMessageGenerator.monitorStatus(dataOtterMonitorsService.getMonitorStatus())).complete();
            config.setMonitoringMessageId(message.getIdLong());
            discordRepository.save(config);
        } else {
            channel.editMessageEmbedsById(config.getMonitoringMessageId(), EmbedMessageGenerator.monitorStatus(dataOtterMonitorsService.getMonitorStatus())).queue();
        }
    }
}
