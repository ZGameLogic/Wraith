package com.zgamelogic.services;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

@Service
public class ModrinthService {
    private final String token;

    private final static String MODRINTH_API_URL = "https://api.modrinth.com/v2";

    public ModrinthService(@Value("${modrinth.api.token}") String modrinthToken) {
        this.token = modrinthToken;
    }

    @PostConstruct
    public void getNotifications(){
        String url = MODRINTH_API_URL + "/user/" + "zabory" + "/notifications";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", token);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        System.out.println(response.getBody());
    }
}
