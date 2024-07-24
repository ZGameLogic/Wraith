package com.zgamelogic.data.api.github.payloads;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Base64;

@Getter
@Setter
@NoArgsConstructor
public class FilePayload {
    private String name;
    private String path;
    private String sha;
    private int size;
    private String url;
    private String html_url;
    private String git_url;
    private String download_url;
    private String content;
    private String type;

    public String decodeContent(){
        byte[] decodedBytes = Base64.getDecoder().decode(content.replace("\n", ""));
        return new String(decodedBytes);
    }
}

