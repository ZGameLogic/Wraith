package com.zgamelogic.data.database.trains;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainRepository extends JpaRepository<TrainLine, String> {
}
