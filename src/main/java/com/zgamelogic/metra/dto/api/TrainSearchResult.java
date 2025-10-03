package com.zgamelogic.metra.dto.api;

import java.time.LocalTime;

public record TrainSearchResult(LocalTime depart, LocalTime arrive, String trainNumber) {}
