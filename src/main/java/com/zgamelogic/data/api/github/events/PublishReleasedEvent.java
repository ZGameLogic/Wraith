package com.zgamelogic.data.api.github.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.zgamelogic.data.api.github.Release;
import com.zgamelogic.data.api.github.Repository;
import com.zgamelogic.data.api.github.User;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PublishReleasedEvent {
    private Release release;
    private Repository repository;
    private User sender;
}
