package com.zgamelogic.metra.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetraCalendar {
    @JsonProperty("service_id")
    private String serviceId;
    @JsonDeserialize(using = CustomBooleanDeserializer.class)
    private Boolean monday;
    @JsonDeserialize(using = CustomBooleanDeserializer.class)
    private Boolean tuesday;
    @JsonDeserialize(using = CustomBooleanDeserializer.class)
    private Boolean wednesday;
    @JsonDeserialize(using = CustomBooleanDeserializer.class)
    private Boolean thursday;
    @JsonDeserialize(using = CustomBooleanDeserializer.class)
    private Boolean friday;
    @JsonDeserialize(using = CustomBooleanDeserializer.class)
    private Boolean saturday;
    @JsonDeserialize(using = CustomBooleanDeserializer.class)
    private Boolean sunday;
    @JsonProperty("start_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyyMMdd")
    private LocalDate startDate;
    @JsonProperty("end_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyyMMdd")
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

    public static class CustomBooleanDeserializer extends JsonDeserializer<Boolean> {

        @Override
        public Boolean deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
            return p.getText().trim().equals("1");
        }
    }
}

