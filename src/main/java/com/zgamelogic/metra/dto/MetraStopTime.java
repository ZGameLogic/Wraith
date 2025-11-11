package com.zgamelogic.metra.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.IOException;
import java.time.LocalTime;

@Getter
@NoArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetraStopTime {
    @JsonProperty("trip_id")
    private String tripId;
    @JsonProperty("arrival_time")
    @JsonDeserialize(using = CustomLocalTimeDeserializer.class)
    private LocalTime arrivalTime;
    @JsonProperty("departure_time")
    @JsonDeserialize(using = CustomLocalTimeDeserializer.class)
    private LocalTime departureTime;
    @JsonProperty("stop_id")
    private String stopId;
    @JsonProperty("stop_sequence")
    private int stopSequence;

    public static class CustomLocalTimeDeserializer extends JsonDeserializer<LocalTime> {
        @Override
        public LocalTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String time = p.getText();
            int hour = Integer.parseInt(time.split(":")[0].trim());
            int minute = Integer.parseInt(time.split(":")[1].trim());
            if (hour >= 24) hour -= 24;
            return LocalTime.of(hour, minute);
        }
    }
}
