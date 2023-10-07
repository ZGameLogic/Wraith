package data.api.github;

import lombok.Data;

@Data
public class Repository {
    private long id;
    private String name;
    private String html_url;
    private String visibility;
    private int open_issues;
}
