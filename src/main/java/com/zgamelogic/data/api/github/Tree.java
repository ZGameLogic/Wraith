package com.zgamelogic.data.api.github;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Tree {
    private String path;
    private String mode;
    private String sha;
    private String url;
    private String type;
}
