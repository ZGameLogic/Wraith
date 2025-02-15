package com.zgamelogic.data.database.trains;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainStopRepository extends JpaRepository<TrainStop, TrainStop.TrainStopId> {
}
