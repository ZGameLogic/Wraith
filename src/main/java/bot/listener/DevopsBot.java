package bot.listener;

import application.App;
import com.zgamelogic.jda.AdvancedListenerAdapter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static com.zgamelogic.jda.Annotations.*;

@Slf4j
@RestController
public class DevopsBot extends AdvancedListenerAdapter {
    @OnReady
    public void ready(ReadyEvent event) {
        Guild guild = event.getJDA().getGuildById(App.config.getGuildId());
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

    @PostMapping("github")
    private void github(@RequestBody String body){
        log.info(body);
    }
}