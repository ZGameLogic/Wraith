package com.zgamelogic.data.database.modrinth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModrinthRepository extends JpaRepository<ModrinthRecord, ModrinthRecord.ModrinthRecordId> {
    List<ModrinthRecord> findAllByProjectId(String project);
    List<ModrinthRecord> findAllByChannelIdAndGuildId(long channelId, long groupId);
}
