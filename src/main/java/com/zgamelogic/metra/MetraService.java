package com.zgamelogic.metra;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.zgamelogic.metra.dto.*;
import com.zgamelogic.metra.dto.api.TrainRouteWithStops;
import com.zgamelogic.metra.dto.api.TrainSearchResult;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.HttpHeaders;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@Slf4j
public class MetraService {
    /*
    https://metra.com/metra-gtfs-api
     */

    private final HttpHeaders headers;
    @Getter
    private final List<MetraRoute> routes;
    private final List<MetraStop> stops;
    private final List<MetraStopTime> stopTimes;
    private final List<MetraCalendar> calendars;
    private final List<MetraTrip> trips;

    public MetraService(@Value("${metra.username}") String username, @Value("${metra.password}") String password) throws IOException {
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
        log.info("Routes: {}", routes.size());
        log.info("Stops: {}", stops.size());
        log.info("Calendars: {}", calendars.size());
        log.info("Trips: {}", trips.size());
        log.info("Stop times: {}", stopTimes.size());
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
                .sorted(Comparator.comparing(MetraStop::getStopName))
                .toList();
    }

    public MetraStop getStopById(String stopId){
        return stops.stream().filter(s -> s.getStopId().equals(stopId)).findFirst().orElse(null);
    }

    public MetraRoute getRouteById(String routeId){
        return routes.stream().filter(r -> r.getRouteId().equals(routeId)).findFirst().orElse(null);
    }

    public List<TrainRouteWithStops> getStopsOnRoutes(){
        List<TrainRouteWithStops> stopsOnRoutes = new ArrayList<>();
        for(MetraRoute route: routes){
            TrainRouteWithStops routeWithStops = new TrainRouteWithStops(route, getStopsOnRouteByRouteId(route.getRouteId(), LocalDate.now()));
            stopsOnRoutes.add(routeWithStops);
        }
        return stopsOnRoutes;
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
    private void updateMetraData() throws IOException {
        routes.clear();
        stops.clear();
        stopTimes.clear();
        calendars.clear();
        trips.clear();

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        byte[] body = restTemplate.exchange("https://schedules.metrarail.com/gtfs/schedule.zip", HttpMethod.GET, requestEntity, byte[].class).getBody();
        ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(body), StandardCharsets.UTF_8);
        ZipEntry entry;
        while((entry = zis.getNextEntry()) != null){
            String name = entry.getName().toLowerCase();
            Class<?> clazz = switch(name) {
                case "routes.txt" -> MetraRoute.class;
                case "stops.txt" -> MetraStop.class;
                case "calendar.txt" -> MetraCalendar.class;
                case "stop_times.txt" -> MetraStopTime.class;
                case "trips.txt" -> MetraTrip.class;
                default -> null;
            };
            if(clazz == null) continue;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int len;
            while ((len = zis.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            zis.closeEntry();
            byte[] entryBytes = baos.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(entryBytes);
            InputStreamReader isr = new InputStreamReader(bais, StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            CsvMapper mapper = new CsvMapper();
            CsvSchema schema = CsvSchema.emptySchema().withHeader();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            mapper.enable(com.fasterxml.jackson.dataformat.csv.CsvParser.Feature.TRIM_SPACES);
            MappingIterator<?> it = mapper.readerFor(clazz).with(schema).readValues(br);
            List<?> items = it.readAll();
            if (clazz.equals(MetraRoute.class)) {
                @SuppressWarnings("unchecked")
                List<MetraRoute> r = (List<MetraRoute>) items;
                routes.addAll(r);
            } else if (clazz.equals(MetraStop.class)) {
                @SuppressWarnings("unchecked")
                List<MetraStop> s = (List<MetraStop>) items;
                stops.addAll(s);
            } else if (clazz.equals(MetraCalendar.class)) {
                @SuppressWarnings("unchecked")
                List<MetraCalendar> c = (List<MetraCalendar>) items;
                calendars.addAll(c);
            } else if (clazz.equals(MetraStopTime.class)) {
                @SuppressWarnings("unchecked")
                List<MetraStopTime> st = (List<MetraStopTime>) items;
                stopTimes.addAll(st);
            } else {
                @SuppressWarnings("unchecked")
                List<MetraTrip> t = (List<MetraTrip>) items;
                trips.addAll(t);
            }
        }
    }
}
