package application;

import com.zgamelogic.boot.JDASpringApplication;
import data.ConfigLoader;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Properties;

@SpringBootApplication(scanBasePackages = {"bot", "data"})
@EnableJpaRepositories({"data.database"})
@EntityScan({"data.database"})
@EnableScheduling
@Slf4j
public class App {
    public static void main(String[] args) {

        // Load config
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.scan("data");
        context.refresh();
        ConfigLoader config = context.getBean(ConfigLoader.class);
        context.close();

        Properties props = new Properties();
        props.setProperty("server.port", config.getWebHookPort() + "");
        props.setProperty("logging.level.root", "INFO");

        // Create bot
        JDABuilder bot = JDABuilder.createDefault(config.getBotToken());
        bot.enableIntents(GatewayIntent.MESSAGE_CONTENT);
        bot.setEventPassthrough(true);

        JDASpringApplication app = new JDASpringApplication(bot, App.class);

        // SSL stuff
        if(config.isUseSSL()) {
            log.info("Turning SSL on");
            props.setProperty("server.ssl.enabled", config.isUseSSL() + "");
            props.setProperty("server.ssl.key-store", config.getKeystoreLocation());
            props.setProperty("server.ssl.key-alias", "tomcat");
            props.setProperty("server.ssl.key-store-password", config.getKeystorePassword());
        }

        // Stuff for SQL
        props.setProperty("spring.datasource.url", "jdbc:sqlserver://zgamelogic.com;databaseName=" + config.getDatabaseName() + ";encrypt=true;trustServerCertificate=true;");
        props.setProperty("spring.datasource.username", config.getSqlUsername());
        props.setProperty("spring.datasource.password", config.getSqlPassword());
        props.setProperty("spring.datasource.driver-class-name", "com.microsoft.sqlserver.jdbc.SQLServerDriver");
        props.setProperty("spring.jpa.hibernate.ddl-auto", "update");
        props.setProperty("spring.jpa.show-sql", "true");
        props.setProperty("org.hibernate.dialect.MySQLInnoDBDialect", "true");
        props.setProperty("spring.jpa.properties.hibernate.enable_lazy_load_no_trans", "true");
        props.setProperty("spring.jpa.properties.hibernate.show_sql", "false");

        app.setDefaultProperties(props);
        app.run(args);
    }
}
