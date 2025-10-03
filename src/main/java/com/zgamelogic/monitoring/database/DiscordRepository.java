package com.zgamelogic.monitoring.database;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DiscordRepository extends JpaRepository<ServerConfig, Long> { }
