package com.zgamelogic.devops.database.user;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "github_user")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GithubUser {
    @Id
    private long discordId;
    private long githubId;
    private String githubToken;
}
