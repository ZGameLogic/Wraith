package com.zgamelogic.services;

import com.zgamelogic.data.api.curseforge.CurseforgeMod;
import com.zgamelogic.data.api.curseforge.CurseforgeMods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CurseforgeService {
    private final String token;

    @Autowired
    public CurseforgeService(@Value("${curseforge.api.token}") String curseforgeToken) {
        token = curseforgeToken;
    }

    public CurseforgeMod getCurseforgeMod(long modId){
        String url = "https://api.curseforge.com/v1/mods/" + modId;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("x-api-key", token);
        ResponseEntity<CurseforgeMod> response = restTemplate.exchange(url, HttpMethod.GET,  new HttpEntity<>(headers), CurseforgeMod.class);
        return response.getBody();
    }

    public List<CurseforgeMod> getCurseforgeMods(long...modIds){
        String url = "https://api.curseforge.com/v1/mods/";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("x-api-key", token);
        Map<String, Object> body = new HashMap<>();
        body.put("modIds", modIds);
        return restTemplate.exchange(url, HttpMethod.POST,  new HttpEntity<>(body, headers), CurseforgeMods.class).getBody().getData();
    }
}
