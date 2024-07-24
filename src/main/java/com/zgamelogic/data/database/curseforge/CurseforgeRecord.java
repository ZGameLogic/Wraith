package com.zgamelogic.data.database.curseforge;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Date;

@Accessors(chain = true)
@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "curseforge")
public class CurseforgeRecord {
    @Id
    @GeneratedValue
    private Long id;

    private Long channelId;
    private Long guildId;
    private String projectId;
    private String projectVersionId;
    private Date lastChecked;
    private Date lastUpdated;
    private String name;
    private Boolean mentionable;
}
