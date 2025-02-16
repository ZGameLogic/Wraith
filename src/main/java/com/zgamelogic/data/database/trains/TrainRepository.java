package com.zgamelogic.data.database.trains;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;
import java.util.List;

public interface TrainRepository extends JpaRepository<Train, Train.TrainId> {
    @Query("""
        SELECT t FROM Train t 
        JOIN t.stops s 
        WHERE t.bound = :bound
        AND s.id.name = 'Chicago OTC' 
        AND s.arrivalTime > cast(:time as time) 
        AND t.id.line.name = :line
    """)
    List<Train> findByBoundAndLineAndStopAfterTime(@Param("time") LocalTime time, @Param("line") String line, @Param("bound") Train.Bound bound);
}
