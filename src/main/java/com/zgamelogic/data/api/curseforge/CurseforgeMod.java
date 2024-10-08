package com.zgamelogic.data.api.curseforge;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.zgamelogic.data.deserializers.curseforge.CurseforgeModDeserializer;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = CurseforgeModDeserializer.class)
public class CurseforgeMod {
    private String name;
    private long projectId;
    private String summary;
    private long downloadCount;
    private String logoUrl;
    private String url;
    private long mainFileId;
    private String fileName;
    private String serverFileUrl, serverFileName;
    private long serverFileId;
    private boolean valid;
}
