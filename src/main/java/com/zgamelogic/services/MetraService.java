package com.zgamelogic.services;

import org.springframework.stereotype.Service;

@Service
public class MetraService {
    /*
    https://metra.com/metra-gtfs-api

    Stop names: /schedule/stops
        stop_id, stop_name
    Stop times: /schedule/stop_times
        trip_id, arrival_time, departure_time, stop_id, stop_sequence
    Routes: /schedule/routes
        route_id, route_long_name
    Calendar: /schedule/calendar
        service_id, monday, tuesday, wednesday, thursday, friday, saturday, sunday, start_date, end_date
    Trips: /schedule/trips
        route_id, service_id, trip_id, trip_headsign, direction_id
    TODO
    get auth going, we have access just need to add it to the requests
    essentially hit all those endpoints, gather the data
    make methods to help us read the data into a legible output to be used in discord and also in my own API
     */
}
