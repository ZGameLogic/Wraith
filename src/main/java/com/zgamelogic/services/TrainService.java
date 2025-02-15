package com.zgamelogic.services;

import com.zgamelogic.data.database.trains.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TrainService {
    private final TrainRepository trainRepository;
    private final TrainLineRepository trainLineRepository;
    private final TrainStopRepository trainStopRepository;
}
