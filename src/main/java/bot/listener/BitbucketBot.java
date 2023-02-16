package bot.listener;

import application.App;
import bot.utils.EmbedMessageGenerator;
import com.zgamelogic.AdvancedListenerAdapter;
import data.database.atlassian.jira.projects.Project;
import data.database.atlassian.jira.projects.ProjectRepository;
import interfaces.atlassian.BitbucketInterfacer;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
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
        Optional<Project> project = projectRepository.getProjectByKey(projectKey);
        if(!project.isPresent()){
            event.reply("This can only be used in a project category").setEphemeral(true).queue();
            return;
        }
        if(project.get().getBitbucketChannelId() != null){
            event.reply("This project category already contains a bitbucket repository").setEphemeral(true).queue();
            return;
        }
        String projectSlug = event.getOption("project").getAsString();
        String repoSlug = event.getOption("repo").getAsString();
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
        project.get().setBitbucketChannelId(bbGen.getIdLong());
        project.get().setBitbucketPrChannelId(bbpr.getIdLong());
        JSONObject bbProject = BitbucketInterfacer.getBitbucketRepository(projectSlug, repoSlug);
        project.get().setBitbucketRepoId(bbProject.getLong("id"));
        projectRepository.save(project.get());
        event.reply("Adding bitbucket repository to this project category.\n" +
                App.config.getBitbucketURL() + "projects/" + projectSlug + "/repos/" + repoSlug + "/browse/").queue();
    }

    public void handleBitbucketWebhook(JSONObject body) throws JSONException {
        switch(body.getString("eventKey")) {
            case "repo:refs_changed": // push was made
                refsChanged(body);
                break;
            case "repo:modified":
                break;
            case "pr:opened":
                break;
            case "pr:from_ref_updated": // this one is for the source branch
                break;
            case "pr:reviewer:approved":
                break;
            case "pr:merged":
                break;
            case "pr:declined":
                break;
            default:
                System.out.println(body);
                break;
        }
    }

    private void refsChanged(JSONObject body) throws JSONException {
        switch(body.getJSONArray("changes").getJSONObject(0).getString("type")){
            case "ADD":
                branchCreated(body);
                break;
            case "UPDATE":
                branchPushedTo(body);
                break;
        }
    }

    private void branchCreated(JSONObject body) throws JSONException {
        long id = body.getJSONObject("repository").getLong("id");
        Optional<Project> project = projectRepository.getProjectByBitbucketKey(id);
        if(!project.isPresent()) return;
        TextChannel bbUpdates = bot.getGuildById(App.config.getGuildId()).getTextChannelById(project.get().getBitbucketChannelId());
        bbUpdates.sendMessageEmbeds(EmbedMessageGenerator.bitbucketBranchCreated(body)).queue();
    }

    private void branchPushedTo(JSONObject body) throws JSONException {
        long id = body.getJSONObject("repository").getLong("id");
        Optional<Project> project = projectRepository.getProjectByBitbucketKey(id);
        if(!project.isPresent()) return;
        TextChannel bbUpdates = bot.getGuildById(App.config.getGuildId()).getTextChannelById(project.get().getBitbucketChannelId());
        String commitId = body.getJSONArray("changes").getJSONObject(0).getString("toHash");
        String projectKey = body.getJSONObject("repository").getJSONObject("project").getString("key");
        String repoKey = body.getJSONObject("repository").getString("slug");
        JSONObject commit = BitbucketInterfacer.getBitbucketCommit(projectKey, repoKey, commitId);
        bbUpdates.sendMessageEmbeds(EmbedMessageGenerator.bitbucketPushMade(body, commit)).queue();
    }
}
