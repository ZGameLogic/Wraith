package com.zgamelogic.data.database.trains;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.List;

@Entity
@Getter
public class TrainLine {
    @Id
    private String name;
    @OneToMany(mappedBy = "id.line", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Train> trains;
}
