package com.zgamelogic.devops.dto.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.zgamelogic.devops.dto.Release;
import com.zgamelogic.devops.dto.Repository;
import com.zgamelogic.devops.dto.User;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PublishReleasedEvent {
    private Release release;
    private Repository repository;
    private User sender;
}
