package com.zgamelogic.data.database.trains;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalTime;

@Entity
@Getter
public class TrainStop {
    @EmbeddedId
    private TrainStopId id;
    private int zone;
    private LocalTime arrivalTime;

    @Embeddable
    @Getter
    public static class TrainStopId {
        private String name;
        @MapsId
        @ManyToOne(cascade = CascadeType.ALL)
        @JoinColumns({
            @JoinColumn(name = "line", referencedColumnName = "line"),
            @JoinColumn(name = "train_number", referencedColumnName = "number")
        })
        private Train train;
    }
}
