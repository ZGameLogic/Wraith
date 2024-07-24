package com.zgamelogic.data.api.github.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.zgamelogic.data.api.github.Label;
import com.zgamelogic.data.api.github.Repository;
import com.zgamelogic.data.api.github.User;
import com.zgamelogic.data.deserializers.github.LabelEventDeserializer;
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
