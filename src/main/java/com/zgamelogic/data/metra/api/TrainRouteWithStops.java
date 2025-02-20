package com.zgamelogic.data.metra.api;

import com.zgamelogic.data.metra.MetraRoute;
import com.zgamelogic.data.metra.MetraStop;

import java.util.List;

public record TrainRouteWithStops(MetraRoute route, List<MetraStop> stops) {}