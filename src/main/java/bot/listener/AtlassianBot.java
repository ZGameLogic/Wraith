package bot.listener;

import application.App;
import bot.utils.EmbedMessageGenerator;
import com.zgamelogic.AdvancedListenerAdapter;
import data.database.atlassian.jira.Project;
import data.database.atlassian.jira.ProjectRepository;
import interfaces.atlassian.JiraInterfacer;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.json.JSONException;
import org.json.JSONObject;

import javax.xml.soap.Text;
import java.util.Optional;

@Slf4j
public class AtlassianBot extends AdvancedListenerAdapter {

    private JDA bot;
    private ProjectRepository projectRepository;

    public AtlassianBot(ProjectRepository projectRepository){
        this.projectRepository = projectRepository;
    }

    @Override
    public void onReady(ReadyEvent event) {
        bot = event.getJDA();
        Guild guild = bot.getGuildById(App.config.getGuildId());
        guild.upsertCommand("devops", "All commands having to do with devops")
                .addSubcommands(
                        new SubcommandData("add_project", "Add a jira project to this discord")
                                .addOption(OptionType.STRING, "key", "Key of the project on jira", true),
                        new SubcommandData("create_issue", "Creates a jira issue"),
                        new SubcommandData("create_bug", "Creates a jira bug")
                )
            .queue();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

    }

    public void handleJiraWebhook(JSONObject body) throws JSONException {
        switch (body.getString("webhookEvent")){
            case "project_created":
                projectCreated(body);
                break;
            case "project_deleted":
                projectDeleted(body);
                break;
            case "project_updated":
                projectUpdate(body);
                break;
            case "jira:issue_created":
                issueCreated(body);
                break;
            case "jira:issue_updated":

                break;
            case "comment_created":

                break;
            default:
                System.out.println(body);
                break;
        }
    }

    private void issueCreated(JSONObject body) throws JSONException {
        long projectId = Long.parseLong(body.getJSONObject("issue").getJSONObject("fields").getJSONObject("project").getString("id"));
        Optional<Project> project = projectRepository.findById(projectId);
        if(!project.isPresent()) return;
        bot.getGuildById(App.config.getGuildId()).getTextChannelById(project.get().getJiraChannelId()).sendMessageEmbeds(
                EmbedMessageGenerator.jiraIssueCreated(body)
        ).queue();
    }

    public void handleBitbucketWebhook(JSONObject jsonBody) {}

    public void handleBambooWebhook(JSONObject jsonBody) {}

    @SlashResponse(value = "devops", subCommandName = "add_project")
    private void addProject(SlashCommandInteractionEvent event) throws JSONException {
        String key = event.getOption("key").getAsString();
        JSONObject jiraProject = JiraInterfacer.getProject(key);
        if(jiraProject == null){
            event.reply("No project with that key was found").setEphemeral(true).queue();
            return;
        }
        event.deferReply().queue();
        Project project = new Project(jiraProject);
        createDiscordProject(project);
        event.getHook().sendMessage(project.getProjectName() + " was added to the discord server").queue();
        projectRepository.save(project);
    }

    @SlashResponse(value = "devops", subCommandName = "add_bamboo")
    private void addBamboo(SlashCommandInteractionEvent event){
        // cat.createTextChannel(projectKey + "-bamboo").queue(textChannel -> textChannel.getManager().setTopic("Bamboo channel for project " + projectName + ". This is a log of all Bamboo events that happen for this project.").queue());
    }

    @SlashResponse(value = "devops", subCommandName = "add_bitbucket")
    private void addBitbucket(SlashCommandInteractionEvent event){
        // cat.createTextChannel(projectKey + "-prod-pull-requests").queue(textChannel -> textChannel.getManager().setTopic("Pull requests into master for project " + projectName + " will show up here.").queue());
        //  cat.createTextChannel(projectKey + "-bitbucket").queue(textChannel -> textChannel.getManager().setTopic("Bitbucket channel for project " + projectName + ". This is a log of all Bitbucket events that happen for this project.").queue());
    }

    private void projectUpdate(JSONObject body) throws JSONException {
        String projectName = body.getJSONObject("project").getString("name");
        String projectKey = body.getJSONObject("project").getString("key");
        long projectId = body.getJSONObject("project").getLong("id");
        Optional<Project> project = projectRepository.findById(projectId);
        if(!project.isPresent()) return;
        Guild guild = bot.getGuildById(App.config.getGuildId());
        Category cat = guild.getCategoryById(project.get().getCategoryId());
        if(projectName.equals(project.get().getProjectName()) && projectKey.equals(project.get().getProjectKey())) return;
        cat.getForumChannels().get(0).getManager().setName(projectKey + "-tickets").queue();
        for(TextChannel channel: cat.getTextChannels()){
            channel.getManager()
                    .setName(channel.getName().replace(project.get().getProjectKey().toLowerCase(), projectKey))
                    .setTopic(channel.getTopic().replace(project.get().getProjectName(), projectName))
                    .queue();
        }
        cat.getManager().setName(projectName).queue();
        project.get().setProjectKey(projectKey);
        project.get().setProjectName(projectName);
        projectRepository.save(project.get());
    }

    private void projectCreated(JSONObject body) throws JSONException {
        String projectName = body.getJSONObject("project").getString("name");
        String projectKey = body.getJSONObject("project").getString("key");
        long projectId = body.getJSONObject("project").getLong("id");
        Project project = new Project(body);
        createDiscordProject(project);
        projectRepository.save(project);
    }

    private void projectDeleted(JSONObject body) throws JSONException {
        long projectId = body.getJSONObject("project").getLong("id");
        Optional<Project> project = projectRepository.findById(projectId);
        if(!project.isPresent()) return;
        Guild guild = bot.getGuildById(App.config.getGuildId());
        Category cat = guild.getCategoryById(project.get().getCategoryId());
        for(GuildChannel channel: cat.getChannels()){
            channel.delete().queue();
        }
        cat.delete().queue();
        projectRepository.deleteById(projectId);
    }

    private void createDiscordProject(Project project){
        Guild guild = bot.getGuildById(App.config.getGuildId());
        Category cat = guild.createCategory(project.getProjectName()).complete();
        cat.createTextChannel(project.getProjectKey() + "-general").queue(textChannel -> textChannel.getManager().setTopic("General channel for project " + project.getProjectName() + ".").queue());
        cat.createForumChannel(project.getProjectKey() + "-tickets").complete();
        TextChannel jira = cat.createTextChannel(project.getProjectKey() + "-jira").complete();
        jira.getManager().setTopic("Jira channel for project " + project.getProjectName() + ". This is a log of all Jira events that happen for this project.").queue();
        project.setCategoryId(cat.getIdLong());
        project.setJiraChannelId(jira.getIdLong());
    }
}
