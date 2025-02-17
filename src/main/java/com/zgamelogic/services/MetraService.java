package com.zgamelogic.services;

import com.zgamelogic.data.metra.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.HttpHeaders;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
@Slf4j
public class MetraService {
    /*
    https://metra.com/metra-gtfs-api

    Stop names: /schedule/stops
        stop_id, stop_name
    Stop times: /schedule/stop_times
        trip_id, arrival_time, departure_time, stop_id, stop_sequence
    Routes: /schedule/routes
        route_id, route_long_name, route_color
    Calendar: /schedule/calendar
        service_id, monday, tuesday, wednesday, thursday, friday, saturday, sunday, start_date, end_date
    Trips: /schedule/trips
        route_id, service_id, trip_id, trip_headsign, direction_id
     */

    private final HttpHeaders headers;
    private final static String BASE_URL = "https://gtfsapi.metrarail.com/gtfs/";
    private final List<MetraRoute> routes;
    private final List<MetraStop> stops;
    private final List<MetraStopTime> stopTimes;
    private final List<MetraCalendar> calendars;
    private final List<MetraTrip> trips;

    public MetraService(@Value("${metra.username}") String username, @Value("${metra.password}") String password) {
        headers = new HttpHeaders();
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
        String authHeader = "Basic " + new String(encodedAuth);
        headers.set("Authorization", authHeader);
        routes = new ArrayList<>();
        stops = new ArrayList<>();
        stopTimes = new ArrayList<>();
        calendars = new ArrayList<>();
        trips = new ArrayList<>();
        updateMetraData();
    }

    @Scheduled(cron = "0 0 3 * * *")
    private void updateMetraData() {
        routes.clear();
        stops.clear();
        stopTimes.clear();
        calendars.clear();
        trips.clear();

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        // Routes
        MetraRoute[] routesResponse = restTemplate.exchange(BASE_URL + "/schedule/routes", HttpMethod.GET, requestEntity, MetraRoute[].class).getBody();
        routes.addAll(List.of(routesResponse));
        log.debug("Routes: {}", routes.size());
        // stops
        MetraStop[] stopsResponse = restTemplate.exchange(BASE_URL + "/schedule/stops", HttpMethod.GET, requestEntity, MetraStop[].class).getBody();
        stops.addAll(List.of(stopsResponse));
        log.debug("Stops: {}", stops.size());
        // stop times
        MetraStopTime[] stopTimesResponse = restTemplate.exchange(BASE_URL + "/schedule/stop_times", HttpMethod.GET, requestEntity, MetraStopTime[].class).getBody();
        stopTimes.addAll(List.of(stopTimesResponse));
        log.debug("Stop Times: {}", stopTimes.size());
        // calendars
        MetraCalendar[] calendarsResponse = restTemplate.exchange(BASE_URL + "/schedule/calendar", HttpMethod.GET, requestEntity, MetraCalendar[].class).getBody();
        calendars.addAll(List.of(calendarsResponse));
        log.debug("Calendars: {}", calendars.size());
        // trips
        MetraTrip[] tripResponse = restTemplate.exchange(BASE_URL + "/schedule/trips", HttpMethod.GET, requestEntity, MetraTrip[].class).getBody();
        trips.addAll(List.of(tripResponse));
        log.debug("Trips: {}", trips.size());
    }
}
