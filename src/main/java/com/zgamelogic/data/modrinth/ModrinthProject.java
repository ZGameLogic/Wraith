package com.zgamelogic.data.modrinth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class ModrinthProject {
    private final String title;
    private final String description;
    private final String id;
    @JsonProperty("icon_url")
    private final String iconUrl;
    private final Double followers;
}
