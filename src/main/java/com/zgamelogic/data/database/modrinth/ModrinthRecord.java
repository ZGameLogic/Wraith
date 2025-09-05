package com.zgamelogic.data.database.modrinth;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Entity
@Getter
@IdClass(ModrinthRecord.ModrinthRecordId.class)
public class ModrinthRecord {
    @Id
    private String projectId;
    @Id
    private Long channelId;
    @Id
    private Long guildId;

    @EqualsAndHashCode
    public static class ModrinthRecordId {
        private String projectId;
        private Long channelId;
        private Long guildId;
    }
}
