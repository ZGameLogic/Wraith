package bot.listener;

import application.App;
import bot.utils.EmbedMessageGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zgamelogic.jda.AdvancedListenerAdapter;
import data.api.atlassian.jira.JiraAPIIssue;
import data.database.atlassian.jira.issues.Issue;
import data.database.atlassian.jira.issues.IssueRepository;
import data.database.atlassian.jira.projects.Project;
import data.database.atlassian.jira.projects.ProjectRepository;
import interfaces.atlassian.JiraInterfacer;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumPost;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Optional;

import static com.zgamelogic.jda.Annotations.*;

@Slf4j
public class JiraBot extends AdvancedListenerAdapter {

    private JDA bot;
    private final ProjectRepository projectRepository;
    private final IssueRepository issueRepository;

    public JiraBot(ProjectRepository projectRepository, IssueRepository issueRepository){
        this.issueRepository = issueRepository;
        this.projectRepository = projectRepository;
    }

    @Override
    public void onReady(ReadyEvent event) {
        bot = event.getJDA();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if(event.getAuthor().isBot()) return;
        Optional<Issue> issue = issueRepository.getIssueByThreadId(event.getChannel().getIdLong());
        if(!issue.isPresent()) return;
        String message = event.getMessage().getContentRaw();
        String user = event.getAuthor().getName();
        JiraInterfacer.sendCommentToIssue(issue.get().getIssueKey(), message, user);
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
                issueUpdate(body);
                commentCreated(body);
                break;
            case "comment_created":
                break;
            default:
                log.info(body.toString());
                break;
        }
    }

    private void commentCreated(JSONObject body) throws JSONException {
        String event = body.getString("issue_event_type_name");
        if(!event.equals("issue_commented")) return;
        String author = body.getJSONObject("comment").getJSONObject("author").getString("name");
        if(author.equals("Wraith")) return;
        long projectId = Long.parseLong(body.getJSONObject("issue").getJSONObject("fields").getJSONObject("project").getString("id"));
        Optional<Project> project = projectRepository.findById(projectId);
        if(!project.isPresent()) return;
        bot.getGuildById(App.config.getGuildId()).getTextChannelById(project.get().getJiraChannelId()).sendMessageEmbeds(
                EmbedMessageGenerator.jiraIssueCommented(body)
        ).queue();
        String key = body.getJSONObject("issue").getString("key");
        Optional<Issue> issue = issueRepository.getIssueByKey(key);
        if(issue.isPresent()){
            bot.getGuildById(App.config.getGuildId()).getThreadChannelById(issue.get().getThreadChannelId()).sendMessageEmbeds(
                    EmbedMessageGenerator.jiraIssueCommented(body)
            ).queue();
        }
    }

    private void issueUpdate(JSONObject body) throws JSONException {
        try {
            JiraAPIIssue jiraIssue = new ObjectMapper().readValue(body.toString(), JiraAPIIssue.class);
            if(!jiraIssue.getIssueEventTypeName().equals("issue_generic")) return;
            long projectId = Long.parseLong(jiraIssue.getIssue().getFields().getProject().getId());
            Optional<Project> project = projectRepository.findById(projectId);
            if(!project.isPresent()) return;
            bot.getGuildById(App.config.getGuildId()).getTextChannelById(project.get().getJiraChannelId()).sendMessageEmbeds(
                    EmbedMessageGenerator.jiraIssueUpdated(jiraIssue)
            ).queue();
            String key = jiraIssue.getIssue().getKey();
            Optional<Issue> issue = issueRepository.getIssueByKey(key);
            if(issue.isPresent()){
                bot.getGuildById(App.config.getGuildId()).getThreadChannelById(issue.get().getThreadChannelId()).sendMessageEmbeds(
                        EmbedMessageGenerator.jiraIssueUpdated(jiraIssue)
                ).queue();
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
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

    @ModalResponse("create_bug")
    private void createBugModal(ModalInteractionEvent event) throws JSONException {
        String projectKey = event.getChannel().getName().split("-")[0].toUpperCase();
        Optional<Project> project = projectRepository.getProjectByKey(projectKey);
        if(!project.isPresent()){
            event.reply("Unable to find project key").setEphemeral(true).queue();
            return;
        }
        String summary = event.getValue("summary").getAsString();
        String description = event.getValue("description").getAsString();
        JSONObject result = JiraInterfacer.createBug(projectKey, summary, description, event.getUser().getName(), event.getUser().getId());
        String key = result.getString("key");
        long id = Long.parseLong(result.getString("id"));
        ForumChannel forumChannel = event.getGuild().getForumChannelById(project.get().getForumChannelId());
        ForumPost post = forumChannel.createForumPost(key, MessageCreateData.fromContent(
                App.config.getJiraURL() + "browse/" + key + "\n" +
                        "Summary: " + summary + "\n" +
                        "Description: " + description + "\n" +
                        "Discord name: " + event.getUser().getName())
        ).complete();
        post.getThreadChannel().addThreadMember(event.getMember()).queue();
        Issue issue = new Issue(id, key, post.getThreadChannel().getIdLong());
        issueRepository.save(issue);
        event.reply("Bug has been submitted").setEphemeral(true).queue();
    }

    @ModalResponse("create_issue")
    private void createIssueModal(ModalInteractionEvent event) throws JSONException {
        String projectKey = event.getChannel().getName().split("-")[0].toUpperCase();
        Optional<Project> project = projectRepository.getProjectByKey(projectKey);
        if(!project.isPresent()){
            event.reply("Unable to find project key").setEphemeral(true).queue();
            return;
        }
        String summary = event.getValue("summary").getAsString();
        String description = event.getValue("description").getAsString();
        String inputLabels = event.getValue("labels").getAsString();
        String[] labels = inputLabels.split(" ");
        JSONObject result = JiraInterfacer.createTask(projectKey, summary, description, labels, event.getUser().getName(), event.getUser().getId());
        event.reply("Issue has been created: " + App.config.getJiraURL() + "browse/" + result.getString("key")).queue();
    }

    @SlashResponse(value = "devops", subCommandName = "create_bug")
    private void createBug(SlashCommandInteractionEvent event){
        String projectKey = event.getChannel().getName().split("-")[0].toUpperCase();
        Optional<Project> project = projectRepository.getProjectByKey(projectKey);
        if(!project.isPresent()){
            event.reply("Unable to find project key").setEphemeral(true).queue();
            return;
        }
        TextInput summary = TextInput.create("summary", "Summary", TextInputStyle.SHORT).setRequired(true).build();
        TextInput description = TextInput.create("description", "Description", TextInputStyle.PARAGRAPH).setRequired(false).build();
        event.replyModal(Modal.create("create_bug", "Create bug report")
                .addActionRow(summary)
                .addActionRow(description)
                .build()).queue();
    }

    @SlashResponse(value = "devops", subCommandName = "create_issue")
    private void createIssue(SlashCommandInteractionEvent event) {
        String projectKey = event.getChannel().getName().split("-")[0].toUpperCase();
        Optional<Project> project = projectRepository.getProjectByKey(projectKey);
        if(!project.isPresent()){
            event.reply("Unable to find project key").setEphemeral(true).queue();
            return;
        }
        TextInput summary = TextInput.create("summary", "Summary", TextInputStyle.SHORT).setRequired(true).build();
        TextInput description = TextInput.create("description", "Description", TextInputStyle.PARAGRAPH).setRequired(false).build();
        TextInput labels = TextInput.create("labels", "Labels", TextInputStyle.SHORT)
                .setPlaceholder("spaced delimited").setRequired(false).build();
        event.replyModal(Modal.create("create_issue", "Create issue")
                .addActionRow(summary)
                .addActionRow(description)
                .addActionRow(labels)
                .build()).queue();
    }

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

    @SlashResponse(value = "devops", subCommandName = "remove_project")
    private void removeProject(SlashCommandInteractionEvent event){
        event.deferReply().queue();
        String key = event.getOption("key").getAsString();
        projectDeleted(key);
        event.getHook().sendMessage("Project was removed from the server").queue();
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
        Project project = new Project(body);
        createDiscordProject(project);
        projectRepository.save(project);
    }

    private void projectDeleted(String key) {
        Optional<Project> project = projectRepository.getProjectByKey(key);
        if(!project.isPresent()) return;
        Guild guild = bot.getGuildById(App.config.getGuildId());
        Category cat = guild.getCategoryById(project.get().getCategoryId());
        for(GuildChannel channel: cat.getChannels()){
            channel.delete().queue();
        }
        cat.delete().queue();
        projectRepository.deleteById(project.get().getProjectId());
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
        ForumChannel forumChannel = cat.createForumChannel(project.getProjectKey() + "-tickets").complete();
        TextChannel jira = cat.createTextChannel(project.getProjectKey() + "-jira").complete();
        jira.getManager().setTopic("Jira channel for project " + project.getProjectName() + ". This is a log of all Jira events that happen for this project.").queue();
        project.setCategoryId(cat.getIdLong());
        project.setForumChannelId(forumChannel.getIdLong());
        project.setJiraChannelId(jira.getIdLong());
    }
}
