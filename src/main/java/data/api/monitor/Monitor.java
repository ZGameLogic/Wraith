package data.api.monitor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

@Getter
@NoArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Monitor {
    private String name;
    private String type;
    private Status[] status;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Status {
        private Date taken;
        private boolean status;
        private long completedInMilliseconds;

        private Integer online;
        private String[] onlinePlayers;
    }

    public boolean getStatus(){
        return status.length > 0 && status[0].isStatus();
    }

    public int getOnline(){
        return status[0].getOnline();
    }

    public Collection<String> getOnlinePlayers() {
        return Arrays.asList(status[0].getOnlinePlayers());
    }
}
