package com.zgamelogic.services;

import com.zgamelogic.data.modrinth.ModrinthNotification;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Service
public class ModrinthService {
    private final String token;

    private final static String MODRINTH_API_URL = "https://api.modrinth.com/v2";

    public ModrinthService(@Value("${modrinth.api.token}") String modrinthToken) {
        this.token = modrinthToken;
    }

    @PostConstruct
    public List<ModrinthNotification> getNotifications(){
        String url = MODRINTH_API_URL + "/user/" + "zabory" + "/notifications";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", token);
        ResponseEntity<ModrinthNotification[]> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), ModrinthNotification[].class);
        System.out.println(Arrays.toString(response.getBody()));
        return List.of(response.getBody());
    }

    @Async
    public void deleteNotifications(Collection<String> notificationIds){
        String url = UriComponentsBuilder.fromUriString(MODRINTH_API_URL + "/notifications")
            .queryParam("ids", String.join(",", notificationIds))
            .toUriString();
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", token);
        restTemplate.exchange(url, HttpMethod.DELETE, new HttpEntity<>(headers), ModrinthNotification[].class);
    }
}
