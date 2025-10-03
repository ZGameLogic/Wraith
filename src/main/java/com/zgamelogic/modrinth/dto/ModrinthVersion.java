package com.zgamelogic.modrinth.dto;

import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class ModrinthVersion {
    private String name;
    private List<File> files;

    @Getter
    @ToString
    public static class File {
        private String url;
        private String filename;
        private String version_number;
    }
}
