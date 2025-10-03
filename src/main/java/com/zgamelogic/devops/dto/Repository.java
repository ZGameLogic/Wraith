package com.zgamelogic.devops.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Repository {
    private long id;
    private String name;
    private String html_url;
    private String labels_url;
    private String visibility;
    private int open_issues;
}
