package bot.listener;

import application.App;
import bot.utils.EmbedMessageGenerator;
import com.zgamelogic.AdvancedListenerAdapter;
import data.database.atlassian.jira.projects.BitbucketProject;
import data.database.atlassian.jira.projects.Project;
import data.database.atlassian.jira.projects.ProjectRepository;
import data.database.atlassian.jira.projects.PullRequest;
import interfaces.atlassian.BitbucketInterfacer;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Optional;

@Slf4j
public class BitbucketBot extends AdvancedListenerAdapter {

    private JDA bot;
    private final ProjectRepository projectRepository;

    public BitbucketBot(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    public void onReady(ReadyEvent event) {
        bot = event.getJDA();
    }

    @SlashResponse(value = "devops", subCommandName = "bb_link")
    private void linkBitbucket(SlashCommandInteractionEvent event) throws JSONException {
        String projectKey = event.getChannel().getName().split("-")[0].toUpperCase();
        String projectSlug = event.getOption("project").getAsString();
        OptionMapping repoMapping = event.getOption("repo");

        Optional<Project> project = projectRepository.getProjectByKey(projectKey);
        if(!project.isPresent()){
            event.reply("This can only be used in a project category").setEphemeral(true).queue();
            return;
        }

        boolean createNewChannel = project.get().getBitbucketProjects().size() < 1;
        try {
            createNewChannel = event.getOption("create_channel").getAsBoolean();
        } catch(Exception ignored){}

        if (repoMapping != null) {
            String repoSlug = repoMapping.getAsString();
            if(createBitbucketOnDiscord(project.get(), projectSlug, repoSlug, createNewChannel)){
                event.reply("Adding bitbucket repository to this project category.\n" +
                        App.config.getBitbucketURL() + "projects/" + projectSlug + "/repos/" + repoSlug + "/browse/").queue();
            } else {
                event.reply("Unable to add the bitbucket repository").queue();
            }
        } else {
            JSONArray repos = BitbucketInterfacer.getBitbucketProjectRepos(projectSlug).getJSONArray("values");
            for(int i = 0; i < repos.length(); i++){
                JSONObject repo = repos.getJSONObject(i);
                // TODO go through all the repos
            }
        }
    }

    private boolean createBitbucketOnDiscord(Project project, String projectSlug, String repoSlug, boolean createNewChannel) throws JSONException {
        for(BitbucketProject bp: project.getBitbucketProjects()){
            if (bp.getProjectSlug().equals(projectSlug) && bp.getRepoSlug().equals(repoSlug)) {
                return false;
            }
        }

        JSONObject response = BitbucketInterfacer.createWebhook(projectSlug, repoSlug);
        if(!response.has("id")) {
            return false;
        }
        Category cat = bot.getGuildById(App.config.getGuildId()).getCategoryById(project.getCategoryId());
        TextChannel bbGen;
        if(createNewChannel) {
            bbGen = cat.createTextChannel(repoSlug).complete();
            bbGen.getManager().setTopic("Channel for bitbucket events for this project").queue();
        } else {
            bbGen = bot.getGuildById(App.config.getGuildId()).getTextChannelById(project.getBitbucketProjects().get(0).getChannelId());
        }
        TextChannel bbpr = cat.createTextChannel(repoSlug + "-pull-requests").complete();
        bbpr.getManager().setTopic("Channel for bitbucket pul requests for this project").queue();
        BitbucketProject newProject = new BitbucketProject();
        newProject.setChannelId(bbGen.getIdLong());
        newProject.setPullRequestChannelId(bbpr.getIdLong());
        JSONObject bbProject = BitbucketInterfacer.getBitbucketRepository(projectSlug, repoSlug);
        newProject.setRepositoryId(bbProject.getLong("id"));
        newProject.setProjectSlug(bbProject.getJSONObject("project").getString("key"));
        newProject.setRepoSlug(bbProject.getString("slug"));
        project.getBitbucketProjects().add(newProject);
        projectRepository.save(project);
        return true;
    }

    @ButtonResponse("merge")
    private void merge(ButtonInteractionEvent event) throws JSONException {
        event.deferReply().queue();
        Optional<Project> jiraProject = projectRepository.getJiraProjectByBitbucketPrChannelId(event.getChannel().getIdLong());
        if(!jiraProject.isPresent()) {
            event.getHook().sendMessage("Unable to find project").setEphemeral(true).queue();
            return;
        }
        Optional<BitbucketProject> project = jiraProject.get().getBitbucketRepoByPrChannelId(event.getChannel().getIdLong());
        if(!project.isPresent()){
            event.getHook().sendMessage("Unable to find project").setEphemeral(true).queue();
            return;
        }
        String projectSlug = project.get().getProjectSlug();
        String repoSlug = project.get().getRepoSlug();
        MessageEmbed message = event.getMessage().getEmbeds().get(0);
        String from = message.getTitle().replace("Push was made to branch: ", "");
        String to = event.getButton().getLabel().replace("Merge into ", "");
        JSONObject pullRequest = BitbucketInterfacer.createPullRequest(projectSlug, repoSlug, from, to);
        long version = pullRequest.getLong("version");
        long prId = pullRequest.getLong("id");
        BitbucketInterfacer.mergePullRequest(projectSlug, repoSlug, prId, version);
        event.getMessage().editMessageComponents(
                event.getMessage().getComponents().get(0).asDisabled()
        ).queue();
        event.getHook().sendMessage("Created a pull request and merged into master").queue();
    }

    public void handleBitbucketWebhook(JSONObject body) throws JSONException {
        switch(body.getString("eventKey")) {
            case "repo:refs_changed": // push was made
                for(int i = 0; i < body.getJSONArray("changes").length(); i++) {
                    JSONObject change = body.getJSONArray("changes").getJSONObject(i);
                    switch (change.getString("type")) {
                        case "ADD":
                            branchCreated(body, i);
                            break;
                        case "UPDATE":
                            branchPushedTo(body, i);
                            break;
                    }
                }
                break;
            case "pr:opened":
                prCreated(body);
                break;
            case "pr:merged":
                prMerged(body);
                break;
            default:
                System.out.println(body);
                break;
        }
    }

    private void prMerged(JSONObject body) throws JSONException {
        long id = body.getJSONObject("pullRequest").getJSONObject("fromRef").getJSONObject("repository").getLong("id");
        Optional<Project> jiraProject = projectRepository.getJiraProjectByBitbucketRepoId(id);
        if(!jiraProject.isPresent()) return;
        Optional<BitbucketProject> project = jiraProject.get().getBitbucketRepo(id);
        if(!project.isPresent()) return;
        TextChannel bbUpdates = bot.getGuildById(App.config.getGuildId()).getTextChannelById(project.get().getChannelId());
        bbUpdates.sendMessageEmbeds(EmbedMessageGenerator.bitbucketPrMerged(body)).queue();
    }

    private void prCreated(JSONObject body) throws JSONException {
        long id = body.getJSONObject("pullRequest").getJSONObject("fromRef").getJSONObject("repository").getLong("id");
        Optional<Project> jiraProject = projectRepository.getJiraProjectByBitbucketRepoId(id);
        if(!jiraProject.isPresent()) return;
        Optional<BitbucketProject> project = jiraProject.get().getBitbucketRepo(id);
        if(!project.isPresent()) return;
        TextChannel bbUpdates = bot.getGuildById(App.config.getGuildId()).getTextChannelById(project.get().getChannelId());
        bbUpdates.sendMessageEmbeds(EmbedMessageGenerator.bitbucketPrCreate(body)).queue();
    }

    private void branchCreated(JSONObject body, int index) throws JSONException {
        long id = body.getJSONObject("repository").getLong("id");
        Optional<Project> jiraProject = projectRepository.getJiraProjectByBitbucketRepoId(id);
        if(!jiraProject.isPresent()) return;
        Optional<BitbucketProject> project = jiraProject.get().getBitbucketRepo(id);
        if(!project.isPresent()) return;
        TextChannel bbUpdates = bot.getGuildById(App.config.getGuildId()).getTextChannelById(project.get().getChannelId());
        bbUpdates.sendMessageEmbeds(EmbedMessageGenerator.bitbucketBranchCreated(body, index)).queue();
    }

    private void branchPushedTo(JSONObject body, int index) throws JSONException {
        long id = body.getJSONObject("repository").getLong("id");
        Optional<Project> jiraProject = projectRepository.getJiraProjectByBitbucketRepoId(id);
        if(!jiraProject.isPresent()) return;
        Optional<BitbucketProject> project = jiraProject.get().getBitbucketRepo(id);
        if(!project.isPresent()) return;
        TextChannel bbUpdates = bot.getGuildById(App.config.getGuildId()).getTextChannelById(project.get().getChannelId());
        String commitId = body.getJSONArray("changes").getJSONObject(index).getString("toHash");
        String projectKey = body.getJSONObject("repository").getJSONObject("project").getString("key");
        String repoKey = body.getJSONObject("repository").getString("slug");
        String branchName = body.getJSONArray("changes").getJSONObject(index).getJSONObject("ref").getString("displayId");
        JSONObject commit = BitbucketInterfacer.getBitbucketCommit(projectKey, repoKey, commitId);
        MessageEmbed push = EmbedMessageGenerator.bitbucketPushMade(body, commit, index);
        bbUpdates.sendMessageEmbeds(push).queue();

        String branch = body.getJSONArray("changes").getJSONObject(index).getJSONObject("ref").getString("displayId");
        if(!branch.equals("development") && !branch.equals("cert")) return;
        JSONArray branches = BitbucketInterfacer.getBitbucketBranches(projectKey, repoKey).getJSONArray("values");
        boolean cert = false;
        for(int i = 0; i < branches.length(); i++){
            JSONObject jsonBranch = branches.getJSONObject(i);
            if(jsonBranch.getString("displayId").equals("cert")){
                cert = true;
                break;
            }
        }
        String toBranch = cert ? (branch.equals("development") ? "cert" : "master") : "master";
        TextChannel bbPr = bot.getGuildById(App.config.getGuildId()).getTextChannelById(project.get().getPullRequestChannelId());
        Optional<PullRequest> pullRequest = jiraProject.get().getPrMessageId(repoKey, branch);
        pullRequest.ifPresent(pullRequestMessageId -> {
            Message message = bbPr.retrieveMessageById(pullRequestMessageId.getRecentPrMessageId()).complete();
            message.editMessageComponents().queue();
        });
        Message message = bbPr.sendMessageEmbeds(push).addActionRow(
                Button.primary("merge", "Merge into " + toBranch)
        ).complete();
        if(pullRequest.isPresent()){
            pullRequest.get().setRecentPrMessageId(message.getIdLong());
        } else {
            pullRequest = Optional.of(new PullRequest(message.getIdLong(), branchName, repoKey));
        }
        jiraProject.get().updateBitbucketRepo(project.get());
        jiraProject.get().updatePullRequest(pullRequest.get());
        projectRepository.save(jiraProject.get());
    }
}
