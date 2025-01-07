package com.zgamelogic.services;

import com.zgamelogic.data.api.dataOtter.Monitor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;

@Service
@Slf4j
public class DataOtterService {

    public List<Monitor> getMonitorStatus(){
        String URL = "http://monitoring.zgamelogic.com:8080/monitors?include-status=true";
        RestTemplate restTemplate = new RestTemplate();
        try {
            return List.of(restTemplate.getForObject(new URI(URL), Monitor[].class));
        } catch (Exception e) {
            log.error("Error fetching monitors", e);
        }
        return List.of();
    }
}
