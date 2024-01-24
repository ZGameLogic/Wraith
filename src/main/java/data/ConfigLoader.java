package data;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("File:wraith.properties")
@Getter
public class ConfigLoader {

    @Value("${bot.token}")
    private String botToken;

    @Value("${guild.id}")
    private String guildId;

    @Value("${use.ssl:false}")
    private boolean useSSL;

    @Value("${Webhook.port:2002}")
    private int webHookPort;

    @Value("${database.name}")
    private String databaseName;

    @Value("${keystore.password}")
    private String keystorePassword;

    @Value("${keystore.location}")
    private String keystoreLocation;

    @Value("${sql.username}")
    private String sqlUsername;

    @Value("${sql.password}")
    private String sqlPassword;

    @Value("${curseforge.api.token}")
    private String curseforgeApiToken;

    @Value("${monitoring.id}")
    private String monitoringId;

    @Value("${general.id}")
    private String generalId;

    @Value("${github.token}")
    private String githubToken;
}
