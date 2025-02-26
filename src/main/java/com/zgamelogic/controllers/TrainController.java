package com.zgamelogic.controllers;

import com.zgamelogic.data.metra.MetraRoute;
import com.zgamelogic.data.metra.MetraStop;
import com.zgamelogic.data.metra.api.TrainRouteWithStops;
import com.zgamelogic.data.metra.api.TrainSearchResult;
import com.zgamelogic.services.MetraService;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("train")
public class TrainController {
    private final MetraService metraService;
    
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

}
