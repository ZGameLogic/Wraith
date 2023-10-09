package data.api.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Sender {
    private String login;
    private long id;
    private String html_url;
    private String url;
    private String avatar_url;
}
