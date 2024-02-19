package bot.helpers;

import bot.utils.EmbedMessageGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import data.api.github.Label;
import data.api.github.Repository;
import data.api.github.WorkflowRun;
import data.api.github.events.*;
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
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumTagData;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.json.JSONObject;
import services.GitHubService;

import java.lang.annotation.*;
import java.util.HashMap;
import java.util.LinkedList;

@Slf4j
public abstract class DevopsBotHelper {

    @GithubEvent("push")
    public static void gitHubPush(PushEvent event, GithubRepository gitHubRepositories, Guild glacies, long generalId){
        glacies.getTextChannelById(generalId).sendMessageEmbeds(
                EmbedMessageGenerator.gitHubPush(event)
        ).queue();
        gitHubRepositories.findById(event.getRepository().getId()).ifPresent(discordGithubRepo ->
                glacies.getTextChannelById(discordGithubRepo.getGeneralId()).sendMessageEmbeds(
                        EmbedMessageGenerator.gitHubPush(event)
                ).queue());
    }

    @GithubEvent(value = "repository", action = "created")
    public static void gitHubRepositoryCreated(String body, GithubRepository gitHubRepositories, Guild glacies, GitHubService service){
        JSONObject jsonRepo = new JSONObject(body).getJSONObject("repository");
        try {
            ObjectMapper mapper = new ObjectMapper();
            Repository repo = mapper.readValue(jsonRepo.toString(), Repository.class);
            createDiscordRepository(
                repo, 
                false,
                gitHubRepositories,
                glacies,
                service
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

    @GithubEvent(value = "workflow_job")
    private static void gitHubWorkflow(WorkflowEvent workflowEvent, GithubRepository gitHubRepositories, WorkflowRepository workflowRepository, Guild glacies, GitHubService service){
        WorkflowRun run = service.getWorkflowRun(workflowEvent.getWorkflowJob().getRunUrl());
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

    @GithubEvent(value = "pull_request_review_comment", action = "created")
    private static void gitHubPullRequestReviewSubmitted(String body, GithubRepository gitHubRepositories, Guild glacies){}

    @GithubEvent(value = "issues", action = "opened")
    private static void gitHubIssueOpened(IssueEvent event, GithubRepository gitHubRepositories, GithubIssueRepository githubIssueRepository, Guild glacies){
        gitHubRepositories.findById(event.getRepository().getId()).ifPresent(githubRepoConfig -> {
            ForumChannel forumChannel = glacies.getForumChannelById(githubRepoConfig.getForumChannelId());
            MessageCreateBuilder mcb = new MessageCreateBuilder();
            mcb.setContent("Issue created: " + event.getIssue().getHtmlUrl() + "\n" + event.getIssue().getBody());
            LinkedList<String> labelNames = new LinkedList<>();
            event.getIssue().getLabels().forEach(label -> labelNames.add(label.getName().toLowerCase()));
            forumChannel.createForumPost("Iss #" + event.getIssue().getNumber() + ":" + event.getIssue().getTitle(), mcb.build()).setTags(
                    forumChannel.getAvailableTags().stream().filter(
                            tag -> labelNames.contains(tag.getName().toLowerCase())
                    ).toList()
            ).queue(forumPost -> {
                GithubIssue ghIssue = new GithubIssue();
                ghIssue.setId(event.getIssue().getId());
                ghIssue.setNumber(event.getIssue().getNumber());
                ghIssue.setForumPostId(forumPost.getThreadChannel().getIdLong());
                githubIssueRepository.save(ghIssue);
                if(event.getIssue().getAssignee() != null) gitHubIssueAssigned(event, githubIssueRepository, glacies);
            });
        });
    }

    @GithubEvent(value = "issues", action = "labeled")
    private static void gitHubIssueLabeled(IssueEvent event, GithubRepository gitHubRepositories, GithubIssueRepository githubIssueRepository, Guild glacies){
        if(event.getSender().getLogin().equals("ZGameLogicBot")) return;
        githubIssueRepository.findById(event.getIssue().getId()).ifPresent(issueConfig ->
            gitHubRepositories.findById(event.getRepository().getId()).ifPresent(githubRepoConfig -> {
                ForumChannel forumChannel = glacies.getForumChannelById(githubRepoConfig.getForumChannelId());
                LinkedList<String> labelNames = new LinkedList<>();
                event.getIssue().getLabels().forEach(label -> labelNames.add(label.getName().toLowerCase()));
                glacies.getThreadChannelById(issueConfig.getForumPostId()).getManager().setAppliedTags(
                        forumChannel.getAvailableTags().stream().filter(
                                tag -> labelNames.contains(tag.getName().toLowerCase())
                        ).toList()
                ).queue();
            })
        );
    }

    @GithubEvent(value = "issues", action = "unlabeled")
    private static void gitHubIssueUnlabeled(IssueEvent event, GithubRepository gitHubRepositories, GithubIssueRepository githubIssueRepository, Guild glacies){
        gitHubIssueLabeled(event, gitHubRepositories, githubIssueRepository, glacies);
    }

    @GithubEvent(value = "issues", action = "assigned")
    private static void gitHubIssueAssigned(IssueEvent event, GithubIssueRepository githubIssueRepository, Guild glacies){
        githubIssueRepository.findById(event.getIssue().getId()).ifPresent(issueConfig ->
            glacies.getThreadChannelById(issueConfig.getForumPostId()).sendMessageEmbeds(
                    EmbedMessageGenerator.githubIssueAssignedMessage(event.getIssue().getAssignee())
            ).queue()
        );
    }

    @GithubEvent(value = "issues", action = "closed")
    private static void gitHubIssueClosed(IssueEvent event, GithubIssueRepository githubIssueRepository, Guild glacies){
        githubIssueRepository.findById(event.getIssue().getId()).ifPresent(issueConfig -> {
            ThreadChannel channel = glacies.getThreadChannelById(issueConfig.getForumPostId());
            channel.sendMessageEmbeds(
                    EmbedMessageGenerator.githubIssueClosedMessage(event.getIssue().getAssignee())
            ).queue();
            channel.getManager().setLocked(true).queue();
        });
    }

    @GithubEvent(value = "issues", action = "reopened")
    private static void gitHubIssueReopened(IssueEvent event, GithubIssueRepository githubIssueRepository, Guild glacies){
        githubIssueRepository.findById(event.getIssue().getId()).ifPresent(issueConfig -> {
            ThreadChannel channel = glacies.getThreadChannelById(issueConfig.getForumPostId());
            channel.sendMessageEmbeds(
                    EmbedMessageGenerator.githubIssueReopenedMessage(event.getIssue().getAssignee())
            ).queue();
            channel.getManager().setLocked(false).queue();
        });
    }

    @GithubEvent(value = "issue_comment", action = "created")
    private static void gitHubIssueCommentCreated(IssueCommentEvent event, GithubIssueRepository githubIssueRepository, Guild glacies){
        githubIssueRepository.findById(event.getIssue().getId()).ifPresent(issueConfig -> {
            ThreadChannel channel = glacies.getThreadChannelById(issueConfig.getForumPostId());
            channel.sendMessageEmbeds(
                    EmbedMessageGenerator.githubIssueCommentedMessage(event.getComment())
            ).queue();
            channel.getManager().setLocked(false).queue();
        });
    }

    /**
     * Update the glacies discord server by creating new channels for the git repository
     * @param repository GitHub repository to be updated
     */
    public static void createDiscordRepository(Repository repository, boolean withLabels, GithubRepository gitHubRepositories, Guild glacies, GitHubService service){
        long id = repository.getId();
        if(gitHubRepositories.existsById(id)) return; // No need to make it if it's already there
        if(!glacies.getTextChannelsByName(repository.getName() + " general", true).isEmpty()) return;
        String repoName = repository.getName();
        String repoUrl = repository.getHtml_url();
        repoName = repoName.replaceAll("-", " ");
        GitRepo repo = new GitRepo(id, repoName, repoUrl);
        // cat
        Category cat = glacies.createCategory(repoName).complete();
        // general
        TextChannel general = cat.createTextChannel(repoName + " general").complete();
        general.getManager().setTopic("General channel for repository: " + repoName + ".").queue();
        // forum
        ForumChannel forumChannel = cat.createForumChannel(repoName).complete();
        if(withLabels) {
            new Thread(() -> {
                LinkedList<Label> labels = service.getIssueLabels(repository.getLabels_url().replace("{/name}", ""));
                LinkedList<ForumTagData> tags = new LinkedList<>();
                labels.forEach(label -> tags.add(new ForumTagData(label.getName())));
                forumChannel.getManager().setAvailableTags(tags).queue();
            }, "Add labels").start();
        }

        repo.setCategoryId(cat.getIdLong());
        repo.setGeneralId(general.getIdLong());
        repo.setForumChannelId(forumChannel.getIdLong());

        gitHubRepositories.save(repo);
    }

    /**
     * Update the glacies discord server by deleting channels for a git repository
     * @param id unique identifier of the GitHub repository
     */
    public static void deleteDiscordRepository(long id, GithubRepository gitHubRepositories, Guild glacies){
        gitHubRepositories.findById(id).ifPresent(repo -> {
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
}
