package bot.listener;

import application.App;
import com.zgamelogic.AdvancedListenerAdapter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class DevopsBot extends AdvancedListenerAdapter {
    @Override
    public void onReady(ReadyEvent event) {
        Guild guild = event.getJDA().getGuildById(App.config.getGuildId());
        guild.upsertCommand("devops", "All commands having to do with devops")
                .addSubcommands(
                        new SubcommandData("add_project", "Add a jira project to this discord")
                                .addOption(OptionType.STRING, "key", "Key of the project on jira", true),
                        new SubcommandData("create_issue", "Creates a jira issue"),
                        new SubcommandData("create_bug", "Creates a jira bug"),
                        new SubcommandData("bb_link", "Links a bitbucket repository to this category")
                                .addOption(OptionType.STRING, "project", "Project the repo lives in", true)
                                .addOption(OptionType.STRING, "repo", "Name of the repository", true)
                )
                .queue();
    }
}