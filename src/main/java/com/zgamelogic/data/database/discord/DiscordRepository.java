package com.zgamelogic.data.database.discord;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DiscordRepository extends JpaRepository<ServerConfig, Long> { }
