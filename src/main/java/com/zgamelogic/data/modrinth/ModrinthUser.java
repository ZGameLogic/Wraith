package com.zgamelogic.data.modrinth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ModrinthUser {
    private final String username;
    private final String name;
    private final String id;
}
