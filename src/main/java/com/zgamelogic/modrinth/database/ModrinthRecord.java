package com.zgamelogic.modrinth.database;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@IdClass(ModrinthRecord.ModrinthRecordId.class)
public class ModrinthRecord {
    @Id
    private String projectId;
    @Id
    private Long channelId;
    private String projectName;

    @EqualsAndHashCode
    public static class ModrinthRecordId {
        private String projectId;
        private Long channelId;
    }
}
