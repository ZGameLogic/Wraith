package data.api.monitor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@NoArgsConstructor
@ToString
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class MinecraftMonitor extends Monitor{

    private String name;
    private int online;
    private List<String> onlinePlayers;
    private String version;
    private String motd;
}
