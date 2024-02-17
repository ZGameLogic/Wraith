package data.api.curseforge;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CurseforgeLatestFile {
    private String displayName;
    private long serverPackFileId;
    private long id;
}
