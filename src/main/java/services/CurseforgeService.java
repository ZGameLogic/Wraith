package services;

import application.App;
import data.api.curseforge.CurseforgeFile;
import data.api.curseforge.CurseforgeMod;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public abstract class CurseforgeService {
    public static CurseforgeMod getCurseforgeMod(long modId){
        String url = "https://api.curseforge.com/v1/mods/" + modId;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("x-api-key", App.config.getCurseforgeApiToken());
        ResponseEntity<CurseforgeMod> response = restTemplate.exchange(url, HttpMethod.GET,  new HttpEntity<>(headers), CurseforgeMod.class);
        return response.getBody();
    }

    public static CurseforgeFile getCurseforgeFile(long modId, long fileId){
        String url = "https://api.curseforge.com/v1/mods/" + modId + "/files/" + fileId;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("x-api-key", App.config.getCurseforgeApiToken());
        ResponseEntity<CurseforgeFile> response = restTemplate.exchange(url, HttpMethod.GET,  new HttpEntity<>(headers), CurseforgeFile.class);
        return response.getBody();
    }
}
