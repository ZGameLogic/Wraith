package com.zgamelogic.services;

import com.zgamelogic.data.api.curseforge.CurseforgeFile;
import com.zgamelogic.data.api.curseforge.CurseforgeMod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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

    public CurseforgeFile getCurseforgeFile(long modId, long fileId){
        String url = "https://api.curseforge.com/v1/mods/" + modId + "/files/" + fileId;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("x-api-key", token);
        ResponseEntity<CurseforgeFile> response = restTemplate.exchange(url, HttpMethod.GET,  new HttpEntity<>(headers), CurseforgeFile.class);
        return response.getBody();
    }
}
