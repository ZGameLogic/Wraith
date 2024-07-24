package com.zgamelogic.data.database.curseforge;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CurseforgeRepository extends JpaRepository<CurseforgeRecord, Long> {

    @Query(value = "select * from curseforge c where c.project_id = :id AND c.guild_id = :guild AND c.channel_id = :channel", nativeQuery = true)
    Optional<CurseforgeRecord> getProjectById(@Param("id") String id, @Param("guild") Long guild, @Param("channel") Long channel);

    @Query(value = "select * from curseforge c where c.guild_id = :guild AND c.channel_id = :channel", nativeQuery = true)
    List<CurseforgeRecord> getProjectsByGuildAndChannel(@Param("guild") Long guild, @Param("channel") Long channel);

    @Query(value = "select * from curseforge c where c.name = :name", nativeQuery = true)
    Optional<CurseforgeRecord> getProjectByName(@Param("name") String name);
}
