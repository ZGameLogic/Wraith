package bot.helpers;

import application.App;
import bot.utils.EmbedMessageGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import data.api.github.Label;
import data.api.github.Repository;
import data.api.github.WorkflowRun;
import data.api.github.events.IssueEvent;
import data.api.github.events.LabelEvent;
import data.api.github.events.PushEvent;
import data.api.github.events.WorkflowEvent;
import data.database.github.Issue.GithubIssue;
import data.database.github.Issue.GithubIssueRepository;
import data.database.github.repository.GitRepo;
import data.database.github.repository.GithubRepository;
import data.database.github.workflow.Workflow;
import data.database.github.workflow.WorkflowRepository;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumTagData;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.json.JSONObject;
import services.GitHubService;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.LinkedList;

@Slf4j
public abstract class DevopsBotHelper {

    @CreateDiscordRepo
    @GithubEvent("push")
    public static void gitHubPush(PushEvent event, GithubRepository gitHubRepositories, Guild glacies){
        gitHubRepositories.findById(event.getRepository().getId()).ifPresent(discordGithubRepo ->
                glacies.getTextChannelById(discordGithubRepo.getGeneralId()).sendMessageEmbeds(
                        EmbedMessageGenerator.gitHubPush(event)
                ).queue());
    }

    @CreateDiscordRepo
    @GithubEvent(value = "repository", action = "created")
    public static void gitHubRepositoryCreated(String body, GithubRepository gitHubRepositories, Guild glacies){
        JSONObject jsonRepo = new JSONObject(body).getJSONObject("repository");
        try {
            ObjectMapper mapper = new ObjectMapper();
            Repository repo = mapper.readValue(jsonRepo.toString(), Repository.class);
            createDiscordRepository(
                repo, 
                false,
                gitHubRepositories,
                glacies
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @GithubEvent(value = "repository", action = "deleted")
    private static void gitHubRepositoryDeleted(String body, GithubRepository gitHubRepositories, Guild glacies){
        deleteDiscordRepository(
                new JSONObject(body).getJSONObject("repository").getLong("id"),
                gitHubRepositories,
                glacies
        );
    }

    @CreateDiscordRepo
    @GithubEvent(value = "label", action = "created")
    private static void githubLabelCreated(LabelEvent event, GithubRepository gitHubRepositories, Guild glacies){
        gitHubRepositories.findById(event.getRepository().getId()).ifPresent(repo -> {
            ForumChannel forum = glacies.getForumChannelById(repo.getForumChannelId());
            if(!forum.getAvailableTagsByName(event.getLabel().getName(), true).isEmpty()) return;
            LinkedList<ForumTagData> tags = new LinkedList<>();
            forum.getAvailableTags().forEach(tag -> tags.add(ForumTagData.from(tag)));
            tags.add(new ForumTagData(event.getLabel().getName()));
            forum.getManager().setAvailableTags(tags).queue();
        });
    }

    @CreateDiscordRepo
    @GithubEvent(value = "label", action = "edited")
    private static void githubLabelEdited(LabelEvent event, GithubRepository gitHubRepositories, Guild glacies){
        gitHubRepositories.findById(event.getRepository().getId()).ifPresent(repo -> {
            ForumChannel forum = glacies.getForumChannelById(repo.getForumChannelId());
            LinkedList<ForumTagData> tags = new LinkedList<>();
            forum.getAvailableTags().forEach(tag -> {
                if(!tag.getName().equals(event.getFrom())) {
                    tags.add(ForumTagData.from(tag));
                } else {
                    ForumTagData editedTag = ForumTagData.from(tag);
                    editedTag.setName(event.getLabel().getName());
                    tags.add(editedTag);
                }
            });
            forum.getManager().setAvailableTags(tags).queue();
        });
    }

    @CreateDiscordRepo
    @GithubEvent(value = "label", action = "deleted")
    public static void githubLabelDeleted(LabelEvent event, GithubRepository gitHubRepositories, Guild glacies){
        gitHubRepositories.findById(event.getRepository().getId()).ifPresent(repo -> {
            ForumChannel forum = glacies.getForumChannelById(repo.getForumChannelId());
            LinkedList<ForumTagData> tags = new LinkedList<>();
            forum.getAvailableTags().forEach(tag -> tags.add(ForumTagData.from(tag)));
            tags.removeIf(tag -> tag.getName().equals(event.getLabel().getName()));
            forum.getManager().setAvailableTags(tags).queue();
        });
    }

    @CreateDiscordRepo
    @GithubEvent(value = "workflow_job")
    private static void gitHubWorkflow(WorkflowEvent workflowEvent, GithubRepository gitHubRepositories, WorkflowRepository workflowRepository, Guild glacies){
        WorkflowRun run = GitHubService.getWorkflowRun(workflowEvent.getWorkflowJob().getRunUrl(), App.config.getGithubToken());
        HashMap<String, Emoji> emojis = new HashMap<>();
        emojis.put("completed success", glacies.getEmojisByName("success", false).get(0));
        emojis.put("completed failure", glacies.getEmojisByName("failure", false).get(0));
        emojis.put("queued null", glacies.getEmojisByName("working", false).get(0));
        emojis.put("completed skipped", glacies.getEmojisByName("nay", false).get(0));
        workflowRepository.findById(workflowEvent.getWorkflowJob().getRunId()).ifPresentOrElse(workflow ->
            glacies.getTextChannelById(workflow.getTextChannelId())
                    .editMessageEmbedsById(workflow.getMessageId(), EmbedMessageGenerator.workflow(run, emojis))
                    .queue()
        , () ->
            gitHubRepositories.findById(workflowEvent.getRepository().getId()).ifPresent(discordRepo ->
                glacies.getTextChannelById(discordRepo.getGeneralId())
                        .sendMessageEmbeds(EmbedMessageGenerator.workflow(run, emojis))
                        .queue(message ->
                            workflowRepository.save(new Workflow(
                                    run.getJobs().getFirst().getRunId(),
                                    message.getChannelIdLong(),
                                    message.getIdLong()
                            ))
                        )
            )
        );
    }

    @GithubEvent(value = "pull_request", action = "opened")
    private static void gitHubPullRequestOpened(String body, GithubRepository gitHubRepositories, Guild glacies){}

    @GithubEvent(value = "pull_request", action = "ready_for_review")
    private static void gitHubPullRequestReadyForReview(String body, GithubRepository gitHubRepositories, Guild glacies){}

    @GithubEvent(value = "pull_request", action = "closed")
    private static void gitHubPullRequestClosed(String body, GithubRepository gitHubRepositories, Guild glacies){}

    @GithubEvent(value = "issues", action = "opened")
    private static void gitHubIssueOpened(IssueEvent event, GithubRepository gitHubRepositories, GithubIssueRepository githubIssueRepository, Guild glacies){
        gitHubRepositories.findById(event.getRepository().getId()).ifPresent(githubRepoConfig -> {
            ForumChannel forumChannel = glacies.getForumChannelById(githubRepoConfig.getForumChannelId());
            MessageCreateBuilder mcb = new MessageCreateBuilder();
            mcb.setContent(event.getIssue().getBody());
            LinkedList<String> labelNames = new LinkedList<>();
            event.getIssue().getLabels().forEach(label -> labelNames.add(label.getName().toLowerCase()));
            forumChannel.createForumPost(event.getIssue().getTitle(), mcb.build()).setTags(
                    forumChannel.getAvailableTags().stream().filter(
                            tag -> labelNames.contains(tag.getName().toLowerCase())
                    ).toList()
            ).queue(forumPost -> {
                GithubIssue ghIssue = new GithubIssue();
                ghIssue.setId(event.getIssue().getId());
                ghIssue.setForumPostId(forumPost.getThreadChannel().getIdLong());
                githubIssueRepository.save(ghIssue);
            });
        });
    }

    @GithubEvent(value = "issues", action = "labeled")
    private static void gitHubIssueLabeled(IssueEvent event, GithubRepository gitHubRepositories, GithubIssueRepository githubIssueRepository, Guild glacies){

    }

    @GithubEvent(value = "issues", action = "assigned")
    private static void gitHubIssueAssigned(String body, GithubRepository gitHubRepositories, Guild glacies){

    }

    @GithubEvent(value = "issues", action = "closed")
    private static void gitHubIssueClosed(String body, GithubRepository gitHubRepositories, Guild glacies){

    }

    @GithubEvent(value = "issue_comment", action = "created")
    private static void gitHubIssueCommentCreated(String body, GithubRepository gitHubRepositories, Guild glacies){

    }

    /**
     * Update the glacies discord server by creating new channels for the git repository
     * @param repository GitHub repository to be updated
     */
    public static void createDiscordRepository(Repository repository, boolean withLabels, GithubRepository gitHubRepositories, Guild glacies){
        long id = repository.getId();
        String repoName = repository.getName();
        String repoUrl = repository.getHtml_url();
        repoName = repoName.replaceAll("-", " ");
        GitRepo repo = new GitRepo(id, repoName, repoUrl);
        // cat
        Category cat = glacies.createCategory(repoName).complete();
        // general
        TextChannel general = cat.createTextChannel(repoName + " general").complete();
        general.getManager().setTopic("General channel for repository: " + repoName + ".").queue();
        // pull request
        TextChannel prChannel = cat.createTextChannel(repoName + " pull-requests").complete();
        prChannel.getManager().setTopic("Pull request channel for repository: " + repoName + ".").queue();
        // forum
        ForumChannel forumChannel = cat.createForumChannel(repoName + "-issues").complete();
        if(withLabels) {
            new Thread(() -> {
                LinkedList<Label> labels = GitHubService.getIssueLabels(repository.getLabels_url().replace("{/name}", ""), App.config.getGithubToken());
                LinkedList<ForumTagData> tags = new LinkedList<>();
                labels.forEach(label -> tags.add(new ForumTagData(label.getName())));
                forumChannel.getManager().setAvailableTags(tags).queue();
            }, "Add labels").start();
        }

        repo.setCategoryId(cat.getIdLong());
        repo.setGeneralId(general.getIdLong());
        repo.setPullRequestId(prChannel.getIdLong());
        repo.setForumChannelId(forumChannel.getIdLong());

        gitHubRepositories.save(repo);
    }

    /**
     * Update the glacies discord server by deleting channels for a git repository
     * @param id unique identifier of the GitHub repository
     */
    public static void deleteDiscordRepository(long id, GithubRepository gitHubRepositories, Guild glacies){
        gitHubRepositories.findById(id).ifPresent(repo -> {
            glacies.getTextChannelById(repo.getPullRequestId()).delete().queue();
            glacies.getTextChannelById(repo.getGeneralId()).delete().queue();
            glacies.getForumChannelById(repo.getForumChannelId()).delete().queue();
            glacies.getCategoryById(repo.getCategoryId()).delete().queue();
            gitHubRepositories.delete(repo);
        });
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface GithubEvent {
        String value();
        String action() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface CreateDiscordRepo {}
}
