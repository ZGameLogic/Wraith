package bot.listener;

import application.App;
import com.zgamelogic.AdvancedListenerAdapter;
import data.database.atlassian.jira.projects.BitbucketProject;
import data.database.atlassian.jira.projects.Project;
import data.database.atlassian.jira.projects.ProjectRepository;
import interfaces.atlassian.BitbucketInterfacer;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.PostConstruct;
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
        String repoSlug = event.getOption("repo").getAsString();
        Optional<Project> project = projectRepository.getProjectByKey(projectKey);
        if(!project.isPresent()){
            event.reply("This can only be used in a project category").setEphemeral(true).queue();
            return;
        }
        boolean alreadyContains = false;
        for(BitbucketProject bp: project.get().getBitbucketProjects()){
            if (bp.getProjectSlug().equals(projectSlug) && bp.getRepoSlug().equals(repoSlug)) {
                alreadyContains = true;
                break;
            }
        }
        if(alreadyContains){
            event.reply("This project category already contains a bitbucket repository").setEphemeral(true).queue();
            return;
        }
        JSONObject response = BitbucketInterfacer.createWebhook(projectSlug, repoSlug);
        if(!response.has("id")) {
            event.reply("Unable to create webhook in bitbucket repository").setEphemeral(true).queue();
            return;
        }
        Category cat = bot.getGuildById(App.config.getGuildId()).getCategoryById(project.get().getCategoryId());
        TextChannel bbGen = cat.createTextChannel(projectKey + "-bitbucket").complete();
        bbGen.getManager().setTopic("Channel for bitbucket events for this project").queue();
        TextChannel bbpr = cat.createTextChannel(projectKey + "-pull-requests").complete();
        bbpr.getManager().setTopic("Channel for bitbucket pul requests for this project").queue();
        BitbucketProject newProject = new BitbucketProject();
        newProject.setChannelId(bbGen.getIdLong());
        newProject.setPullRequestChannelId(bbpr.getIdLong());
        JSONObject bbProject = BitbucketInterfacer.getBitbucketRepository(projectSlug, repoSlug);
        newProject.setRepositoryId(bbProject.getLong("id"));
        newProject.setProjectSlug(bbProject.getJSONObject("project").getString("key"));
        newProject.setRepoSlug(bbProject.getString("slug"));
        project.get().getBitbucketProjects().add(newProject);
        projectRepository.save(project.get());
        event.reply("Adding bitbucket repository to this project category.\n" +
                App.config.getBitbucketURL() + "projects/" + projectSlug + "/repos/" + repoSlug + "/browse/").queue();
    }

//    @ButtonResponse("merge")
//    private void merge(ButtonInteractionEvent event) throws JSONException {
//        event.deferReply().queue();
//        String projectId = event.getChannel().getName().split("-")[0];
//        Optional<Project> project = projectRepository.getProjectByKey(projectId);
//        if(!project.isPresent()){
//            event.getHook().sendMessage("Unable to find project").setEphemeral(true).queue();
//            return;
//        }
//        String projectSlug = project.get().getBitbucketProjectSlug();
//        String repoSlug = project.get().getBitbucketRepoSlug();
//        JSONObject pullRequest = BitbucketInterfacer.createPullRequest(projectSlug, repoSlug);
//        long version = pullRequest.getLong("version");
//        long prId = pullRequest.getLong("id");
//        JSONObject merge = BitbucketInterfacer.mergePullRequest(projectSlug, repoSlug, prId, version);
//        event.getMessage().editMessageComponents(
//                event.getMessage().getComponents().get(0).asDisabled()
//        ).queue();
//        event.getHook().sendMessage("Created a pull request and merged into master").queue();
//    }

    public void handleBitbucketWebhook(JSONObject body) throws JSONException {
        switch(body.getString("eventKey")) {
            case "repo:refs_changed": // push was made
                switch(body.getJSONArray("changes").getJSONObject(0).getString("type")){
                    case "ADD":
                        branchCreated(body);
                        break;
                    case "UPDATE":
                        branchPushedTo(body);
                        break;
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
//        long id = body.getJSONObject("pullRequest").getJSONObject("fromRef").getJSONObject("repository").getLong("id");
//        Optional<Project> project = projectRepository.getProjectByBitbucketKey(id);
//        if(!project.isPresent()) return;
//        TextChannel bbUpdates = bot.getGuildById(App.config.getGuildId()).getTextChannelById(project.get().getBitbucketChannelId());
//        bbUpdates.sendMessageEmbeds(EmbedMessageGenerator.bitbucketPrMerged(body)).queue();
    }

    private void prCreated(JSONObject body) throws JSONException {
//        long id = body.getJSONObject("pullRequest").getJSONObject("fromRef").getJSONObject("repository").getLong("id");
//        Optional<Project> project = projectRepository.getProjectByBitbucketKey(id);
//        if(!project.isPresent()) return;
//        TextChannel bbUpdates = bot.getGuildById(App.config.getGuildId()).getTextChannelById(project.get().getBitbucketChannelId());
//        bbUpdates.sendMessageEmbeds(EmbedMessageGenerator.bitbucketPrCreate(body)).queue();
    }

    private void branchCreated(JSONObject body) throws JSONException {
//        long id = body.getJSONObject("repository").getLong("id");
//        Optional<Project> project = projectRepository.getProjectByBitbucketKey(id);
//        if(!project.isPresent()) return;
//        TextChannel bbUpdates = bot.getGuildById(App.config.getGuildId()).getTextChannelById(project.get().getBitbucketChannelId());
//        bbUpdates.sendMessageEmbeds(EmbedMessageGenerator.bitbucketBranchCreated(body)).queue();
    }

    private void branchPushedTo(JSONObject body) throws JSONException {
//        long id = body.getJSONObject("repository").getLong("id");
//        Optional<Project> project = projectRepository.getProjectByBitbucketKey(id);
//        if(!project.isPresent()) return;
//        TextChannel bbUpdates = bot.getGuildById(App.config.getGuildId()).getTextChannelById(project.get().getBitbucketChannelId());
//        String commitId = body.getJSONArray("changes").getJSONObject(0).getString("toHash");
//        String projectKey = body.getJSONObject("repository").getJSONObject("project").getString("key");
//        String repoKey = body.getJSONObject("repository").getString("slug");
//        JSONObject commit = BitbucketInterfacer.getBitbucketCommit(projectKey, repoKey, commitId);
//        MessageEmbed push = EmbedMessageGenerator.bitbucketPushMade(body, commit);
//        bbUpdates.sendMessageEmbeds(push).queue();
//        boolean devlBranch = body.getJSONArray("changes").getJSONObject(0).getJSONObject("ref").getString("displayId").equals("development");
//        if(devlBranch){
//            TextChannel bbPr = bot.getGuildById(App.config.getGuildId()).getTextChannelById(project.get().getBitbucketPrChannelId());
//            if(project.get().getBitbucketRecentPrMessageId() != null){ // Remove stuff if it exists
//                Message message = bbPr.retrieveMessageById(project.get().getBitbucketRecentPrMessageId()).complete();
//                if(!((Button)(message.getActionRows().get(0).getComponents().get(0))).isDisabled()){
//                    message.editMessageComponents().queue();
//                }
//            }
//            Message message = bbPr.sendMessageEmbeds(push).addActionRow(
//                    Button.primary("merge", "Merge into master")
//            ).complete();
//            project.get().setBitbucketRecentPrMessageId(message.getIdLong());
//            projectRepository.save(project.get());
//        }
    }
}
