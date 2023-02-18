package data.database.atlassian.jira.projects;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.json.JSONException;
import org.json.JSONObject;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Setter
@NoArgsConstructor
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

    private Long bitbucketChannelId;
    private Long bitbucketPrChannelId;
    private Long bitbucketRepoId;
    private String bitbucketProjectSlug;
    private String bitbucketRepoSlug;

    public Project(JSONObject json) throws JSONException {
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
