package com.zgamelogic.devops.dto.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.zgamelogic.devops.dto.Label;
import com.zgamelogic.devops.dto.Repository;
import com.zgamelogic.devops.dto.User;
import com.zgamelogic.devops.dto.LabelEventDeserializer;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = LabelEventDeserializer.class)
public class LabelEvent {
    private Repository repository;
    private Label label;
    private String from;
    private User sender;
}
