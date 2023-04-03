package data.database.atlassian.jira.projects;

import lombok.*;
import org.json.JSONException;
import org.json.JSONObject;

import javax.persistence.*;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
@Entity
@ToString
@Table(name = "jira_projects")
public class Project {

    @Id
    private long projectId;
    private String projectKey;
    private String projectName;
    private Long categoryId;
    private Long jiraChannelId;
    private Long forumChannelId;

//    private Long bitbucketChannelId;
//    private Long bitbucketPrChannelId;
//    private Long bitbucketRecentPrMessageId;
//    private Long bitbucketRepoId;
//    private String bitbucketProjectSlug;
//    private String bitbucketRepoSlug;

    @Column
    @CollectionTable(name = "project_repositories", joinColumns = @JoinColumn(name = "project_id"))
    @ElementCollection
    private List<BitbucketProject> bitbucketProjects;

    public Project(){
        bitbucketProjects = new LinkedList<>();
    }

    public Project(JSONObject json) throws JSONException {
        super();
        if(json.has("project")) {
            JSONObject project = json.getJSONObject("project");
            projectId = project.getLong("id");
            projectName = project.getString("name");
            projectKey = project.getString("key");
        }else{
            projectId = json.getLong("id");
            projectName = json.getString("name");
            projectKey = json.getString("key");
        }
    }
}
