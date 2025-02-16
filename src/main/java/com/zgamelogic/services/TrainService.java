package com.zgamelogic.services;

import com.zgamelogic.data.database.trains.*;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;

import static com.zgamelogic.data.database.trains.Train.Bound.*;

@Service
@AllArgsConstructor
public class TrainService {
    private final TrainRepository trainRepository;
    private final TrainLineRepository trainLineRepository;
    private final TrainStopRepository trainStopRepository;

    @PostConstruct
    public void init(){
        getTrains(LocalTime.of(8, 0), "UPW").forEach(train -> System.out.println(train.getId().getNumber()));
    }

    public List<Train> getTrains(LocalTime time, String line) {
        return trainRepository.findByBoundAndLineAndStopAfterTime(time, line, OUTBOUND);
    }
}
