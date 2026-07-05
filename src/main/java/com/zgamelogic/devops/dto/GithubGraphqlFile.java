package com.zgamelogic.devops.dto;

public record GithubGraphqlFile(
    String oid,
    Long byteSize,
    String text
) {}
