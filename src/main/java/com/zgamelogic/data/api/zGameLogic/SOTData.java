package com.zgamelogic.data.api.zGameLogic;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;
import com.zgamelogic.data.discord.SeaOfThievesEventData;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Getter
@NoArgsConstructor(force = true)
public class SOTData {
    private final long id;
    private final boolean ben;
    private final boolean greg;
    private final boolean jj;
    private final boolean patrick;
    private final boolean success;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS")
    private final LocalDateTime proposed;

    public SOTData(SeaOfThievesEventData data){
        id = 0;
        ben = data.isBen();
        greg = data.isGreg();
        jj = data.isJj();
        patrick = data.isPatrick();
        success = data.isSuccess();
        proposed = parseDateString(data.getTime());
    }

    private LocalDateTime parseDateString(String dateString) {
        Parser parser = new Parser();
        List<DateGroup> groups = parser.parse(dateString);

        if (groups.isEmpty()) return null; // No dates found

        DateGroup group = groups.get(0);
        List<Date> dates = group.getDates();

        return dates.isEmpty() ? null : dates.get(0).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(); // Return the first parsed date
    }
}
