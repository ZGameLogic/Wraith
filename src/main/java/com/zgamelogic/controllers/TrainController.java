package com.zgamelogic.controllers;

import com.zgamelogic.data.database.apple.device.Device;
import com.zgamelogic.data.database.apple.device.DeviceRepository;
import com.zgamelogic.data.database.apple.live.LiveActivity;
import com.zgamelogic.data.database.apple.live.LiveActivityRepository;
import com.zgamelogic.data.metra.api.TrainRouteWithStops;
import com.zgamelogic.data.metra.api.TrainSearchResult;
import com.zgamelogic.services.MetraService;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("train")
public class TrainController {
    private final MetraService metraService;
    private final DeviceRepository deviceRepository;
    private final LiveActivityRepository liveActivityRepository;
    
    @GetMapping("search")
    public ResponseEntity<List<TrainSearchResult>> trainSearch(
        @RequestParam String route,
        @RequestParam String to,
        @RequestParam String from,
        @RequestParam(required = false)
        @DateTimeFormat(pattern = "yyyy-M-d")
        LocalDate date,
        @RequestParam(required = false) LocalTime time
    ){
        if(date == null) date = LocalDate.now();
        if(time == null) time = LocalTime.now();
        List<TrainSearchResult> result = metraService.trainSearch(route, from, to, date, time);
        return ResponseEntity.ok(result);
    }

    @GetMapping("routes")
    public ResponseEntity<List<TrainRouteWithStops>> getRoutes(){
        return ResponseEntity.ok(metraService.getStopsOnRoutes());
    }

    @PostMapping("register/{device}")
    public ResponseEntity<String> registerPhone(@PathVariable String device){
        if(deviceRepository.existsById(device)) return ResponseEntity.ok("Device already registered");
        deviceRepository.save(new Device(device));
        return ResponseEntity.ok("Device registered successfully");
    }

    @PostMapping("register/live/{train}/{token}")
    public ResponseEntity<String> registerLive(@PathVariable String token, @PathVariable String train){
        liveActivityRepository.save(new LiveActivity(token, train));
        return ResponseEntity.ok("Live activity registered successfully");
    }

}
