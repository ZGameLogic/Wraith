package com.zgamelogic.metra.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class MetraCalendar {
    @JsonProperty("service_id")
    private String serviceId;
    private boolean monday;
    private boolean tuesday;
    private boolean wednesday;
    private boolean thursday;
    private boolean friday;
    private boolean saturday;
    private boolean sunday;
    @JsonProperty("start_date")
    private LocalDate startDate;
    @JsonProperty("end_date")
    private LocalDate endDate;

    public boolean isForDay(int dayInt){
        return switch (dayInt) {
            case 1 -> monday;
            case 2 -> tuesday;
            case 3 -> wednesday;
            case 4 -> thursday;
            case 5 -> friday;
            case 6 -> saturday;
            case 7 -> sunday;
            default -> false;
        };
    }

    public boolean isSingleDay(){
        return startDate.equals(endDate);
    }
}

