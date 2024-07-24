package com.zgamelogic.data.api.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Release {
    private String html_url;
    private long id;
    private String tag_name;
    private String name;
    private String body;
    private User author;
}
