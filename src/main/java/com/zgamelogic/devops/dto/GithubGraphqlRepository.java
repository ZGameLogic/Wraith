package com.zgamelogic.devops.dto;

public record GithubGraphqlRepository(
    DefaultBranch defaultBranchRef,
    LatestRelease latestRelease
) {
    public record DefaultBranch(
        String name,
        Target target
    ){}
    public record LatestRelease(
        String tagName,
        String name,
        String description,
        String createdAt,
        String url
    ){}
    public record Target(
        String oid
    ){}
}
