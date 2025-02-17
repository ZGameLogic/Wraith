package com.zgamelogic.services;

import com.zgamelogic.data.database.trains.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;

@Service
@AllArgsConstructor
public class TrainService {
    private final TrainRepository trainRepository;
    private final TrainLineRepository trainLineRepository;
    private final TrainStopRepository trainStopRepository;

    public List<Train> getTrains(LocalTime time, String line, Train.Bound bound) {
        return trainRepository.findByBoundAndLineAndStopAfterTime(time, line, bound);
    }
}
