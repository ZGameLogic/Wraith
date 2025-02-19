package com.zgamelogic.services;

import com.zgamelogic.data.metra.*;
import com.zgamelogic.data.metra.api.TrainSearchResult;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.HttpHeaders;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MetraService {
    /*
    https://metra.com/metra-gtfs-api
     */

    private final HttpHeaders headers;
    private final static String BASE_URL = "https://gtfsapi.metrarail.com/gtfs/";
    @Getter
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
        log.info("Metra train data loaded");
    }

    public List<TrainSearchResult> trainSearch(String routeId, String toStopId, String fromStopId, LocalDate date, LocalTime time) {
        MetraRoute route = routes.stream().filter(r -> r.getRouteId().equals(routeId)).findFirst().orElse(null);
        if(route == null) return List.of();
        List<MetraCalendar> currentCalendars = getCalendarsByDate(date);
        List<MetraTrip> trips = getTripsByCalendarAndRouteId(currentCalendars, routeId);
        List<String> tripIds = trips.stream().map(MetraTrip::getTripId).toList();
        List<MetraStopTime> stopTimes = getStopTimesByTripIds(tripIds);
        List<TrainSearchResult> results = new ArrayList<>();
        for(String tripId: tripIds){
            List<MetraStopTime> stops = stopTimes.stream()
                    .filter(s -> s.getDepartureTime().isAfter(time))
                    .filter(s -> s.getTripId().equals(tripId) && (s.getStopId().equals(toStopId) || s.getStopId().equals(fromStopId)))
                    .sorted(Comparator.comparingInt(MetraStopTime::getStopSequence)).toList();
            if(stops.size() != 2) continue;
            MetraStopTime toStop = stops.get(1);
            MetraStopTime fromStop = stops.get(0);
            if(!toStop.getStopId().equals(fromStopId)) continue;
            String trainNumber = tripId.split("_")[1].replaceAll("[A-Z]", "");
            TrainSearchResult result = new TrainSearchResult(fromStop.getDepartureTime(), toStop.getArrivalTime(), trainNumber);
            results.add(result);
        }
        return results;
    }

    public List<MetraStop> getStopsOnRouteByRouteId(String routeId, LocalDate date){
        List<MetraCalendar> currentCalendars = getCalendarsByDate(date);
        List<MetraTrip> trips = getTripsByCalendarAndRouteId(currentCalendars, routeId);
        List<String> tripIds = trips.stream().map(MetraTrip::getTripId).toList();
        List<MetraStopTime> stopTimes = getStopTimesByTripIds(tripIds);
        List<String> stopIds = stopTimes.stream().map(MetraStopTime::getStopId).toList();
        return stops.stream()
                .filter(s -> stopIds.contains(s.getStopId()))
                .distinct()
                .sorted(Comparator.comparing(s -> s.getStopName()))
                .toList();
    }

    public MetraStop getStopById(String stopId){
        return stops.stream().filter(s -> s.getStopId().equals(stopId)).findFirst().orElse(null);
    }

    public MetraRoute getRouteById(String routeId){
        return routes.stream().filter(r -> r.getRouteId().equals(routeId)).findFirst().orElse(null);
    }

    private List<MetraStopTime> getStopTimesByTripIds(List<String> tripIds){
        return stopTimes.stream().filter(st -> tripIds.contains(st.getTripId())).toList();
    }

    private List<MetraTrip> getTripsByCalendarAndRouteId(List<MetraCalendar> currentCalendars, String routeId){
        List<String> serviceIds = currentCalendars.stream().map(MetraCalendar::getServiceId).toList();
        return trips.stream().filter(t -> t.getRouteId().equals(routeId) && serviceIds.contains(t.getServiceId())).toList();
    }

    private List<MetraCalendar> getCalendarsByDate(LocalDate date){
        int day = date.getDayOfWeek().getValue();
        List<MetraCalendar> currentCalendars = calendars.stream().filter(c -> {
            boolean before = c.getStartDate().isBefore(date) || c.getStartDate().isEqual(date);
            boolean after = c.getEndDate().isAfter(date) || c.getEndDate().isEqual(date);
            boolean forDay = c.isForDay(day);
            return before && after && forDay;
        }).collect(Collectors.toList());
        if(currentCalendars.stream().anyMatch(MetraCalendar::isSingleDay)) {
            currentCalendars.removeIf(c -> !c.getEndDate().equals(date) || !c.getStartDate().equals(date));
        }
        return currentCalendars;
    }

    @Scheduled(cron = "0 0 5 * * *")
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
