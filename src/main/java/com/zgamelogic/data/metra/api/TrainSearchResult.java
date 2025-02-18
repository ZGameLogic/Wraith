package com.zgamelogic.data.metra.api;

import java.time.LocalTime;

public record TrainSearchResult(LocalTime depart, LocalTime arrive, String trainNumber) {}
