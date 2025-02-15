package com.zgamelogic.data.database.trains;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.List;

@Entity
@Getter
public class Train {
    @EmbeddedId
    private TrainId id;
    @Enumerated(EnumType.STRING)
    private Bound bound;
    private boolean maintenance;
    private boolean weekday;

    @OneToMany(mappedBy = "id.train", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TrainStop> stops;

    @Embeddable
    @Getter
    public static class TrainId {
        @ManyToOne(cascade = CascadeType.ALL)
        @JoinColumn(name = "line", referencedColumnName = "name", nullable = false)
        private TrainLine line;
        private String number;
    }

    public enum Bound {
        INBOUND, OUTBOUND
    }
}
