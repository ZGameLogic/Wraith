package com.zgamelogic.modrinth;

import com.zgamelogic.modrinth.dto.ModrinthNotification;
import com.zgamelogic.modrinth.dto.ModrinthProject;
import com.zgamelogic.modrinth.dto.ModrinthUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collection;
import java.util.List;

@Slf4j
@Service
public class ModrinthService {
    private final static String MODRINTH_API_URL = "https://api.modrinth.com/v2";
    private final HttpHeaders headers;
    private final ModrinthUser user;

    public ModrinthService(@Value("${modrinth.api.token}") String modrinthToken) {
        this.headers = new HttpHeaders();
        headers.add("Authorization", modrinthToken);
        user = getUser();
        log.info("Modrinth user: {}", user.getUsername());
    }

    private ModrinthUser getUser() {
        String url = MODRINTH_API_URL + "/user";
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<ModrinthUser> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), ModrinthUser.class);
        return response.getBody();
    }

    public ModrinthProject getProject(String id){
        String url = MODRINTH_API_URL + "/project/" + id;
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<ModrinthProject> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), ModrinthProject.class);
        return response.getBody();
    }

    public void followProject(String id){
        String url = MODRINTH_API_URL + "/project/" + id + "/follow";
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(headers), Void.class);
    }

    public List<ModrinthNotification> getNotifications(){
        String url = MODRINTH_API_URL + "/user/" + user.getId() + "/notifications";
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<ModrinthNotification[]> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), ModrinthNotification[].class);
        return List.of(response.getBody());
    }

    @Async
    public void deleteNotifications(Collection<String> notificationIds){
        String url = UriComponentsBuilder.fromUriString(MODRINTH_API_URL + "/notifications")
            .queryParam("ids", String.join(",", notificationIds))
            .toUriString();
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.exchange(url, HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);
    }
}
