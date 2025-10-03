package com.zgamelogic.modrinth.database;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModrinthRepository extends JpaRepository<ModrinthRecord, ModrinthRecord.ModrinthRecordId> {
    List<ModrinthRecord> findAllByProjectId(String projectId);
    List<ModrinthRecord> findAllByChannelId(long channelId);
    boolean existsByProjectId(String projectId);
    void deleteByProjectIdAndChannelId(String projectId, long channelId);
}
