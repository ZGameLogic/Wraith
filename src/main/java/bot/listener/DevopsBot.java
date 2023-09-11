package bot.listener;

import com.zgamelogic.jda.AdvancedListenerAdapter;
import data.ConfigLoader;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.zgamelogic.jda.Annotations.*;

@RestController
public class DevopsBot extends AdvancedListenerAdapter {

    private final ConfigLoader config;

    @Autowired
    public DevopsBot(ConfigLoader config) {
        this.config = config;
    }

    @OnReady
    private void ready(ReadyEvent event) {
        Guild guild = event.getJDA().getGuildById(config.getGuildId());
        guild.upsertCommand("devops", "All commands having to do with devops")
                .addSubcommands(
                        new SubcommandData("add_project", "Add a jira project to this discord")
                                .addOption(OptionType.STRING, "key", "Key of the project on jira", true),
                        new SubcommandData("remove_project", "Remove a jira project from this discord")
                                .addOption(OptionType.STRING, "key", "Key of the project on jira", true),
                        new SubcommandData("create_issue", "Creates a jira issue"),
                        new SubcommandData("create_bug", "Creates a jira bug"),
                        new SubcommandData("bb_link", "Links a bitbucket repository to this category")
                                .addOption(OptionType.STRING, "project", "Project the repo lives in", true)
                                .addOption(OptionType.STRING, "repo", "Name of the repository", true)
                                .addOption(OptionType.BOOLEAN, "create_channel", "True if it should create a new channel. False to use existing", false)
                )
                .queue();
    }

    @GetMapping("health")
    private String healthCheck(){
        return "Healthy";
    }
}