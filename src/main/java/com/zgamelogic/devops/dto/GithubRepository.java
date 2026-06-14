package com.zgamelogic.devops.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GithubRepository(
        String name,
        @JsonProperty("full_name")
        String fullName,
        @JsonProperty("private")
        boolean isPrivate,
        @JsonProperty("html_url")
        String htmlUrl,
        @JsonProperty("languages_url")
        String languagesUrl,
        @JsonProperty("deployments_url")
        String deploymentsUrl,
        @JsonProperty("releases_url")
        String releasesUrl,
        @JsonProperty("milestones_url")
        String milestonesUrl,
        @JsonProperty("branches_url")
        String branchesUrl,
        String description,
        long id,
        @JsonProperty("stargazers_count")
        long stars,
        @JsonProperty("watchers_count")
        long watchers
) {}
