package com.zgamelogic.data.database.discord;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "discord_config")
@Getter
@Setter
@NoArgsConstructor
public class ServerConfig {
    @Id
    private long id;
    private Long monitoringMessageId;

    public ServerConfig(long id) {
        this.id = id;
    }

    public boolean hasMonitoringMessage(){
        return monitoringMessageId != null;
    }
}
