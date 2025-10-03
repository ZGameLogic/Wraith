package com.zgamelogic.devops.dto.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.zgamelogic.devops.dto.Comment;
import com.zgamelogic.devops.dto.Issue;
import com.zgamelogic.devops.dto.Repository;
import com.zgamelogic.devops.dto.User;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class IssueCommentEvent {
    private Issue issue;
    private Repository repository;
    private User sender;
    private Comment comment;
}
