package com.zgamelogic.data.database.sercets;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "secrets")
@NoArgsConstructor
@Getter
public class Secret {
    @Id
    @GeneratedValue
    private UUID id;
    private String name;
    @Convert(converter = SecretValueConverter.class)
    private String value;
    private long access;

    public Secret(String name, String value, long access) {
        this.name = name;
        this.value = value;
        this.access = access;
    }
}
