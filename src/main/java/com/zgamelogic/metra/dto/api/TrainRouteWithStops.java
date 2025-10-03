package com.zgamelogic.metra.dto.api;

import com.zgamelogic.metra.dto.MetraRoute;
import com.zgamelogic.metra.dto.MetraStop;

import java.util.List;

public record TrainRouteWithStops(MetraRoute route, List<MetraStop> stops) {}
