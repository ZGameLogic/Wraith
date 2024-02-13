package services;

import data.api.curseforge.CurseforgeFile;
import data.api.curseforge.CurseforgeMod;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public abstract class CurseforgeService {
    public static CurseforgeMod getCurseforgeMod(long modId, String token){
        String url = "https://api.curseforge.com/v1/mods/" + modId;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("x-api-key", token);
        ResponseEntity<CurseforgeMod> response = restTemplate.exchange(url, HttpMethod.GET,  new HttpEntity<>(headers), CurseforgeMod.class);
        return response.getBody();
    }

    public static CurseforgeFile getCurseforgeFile(long modId, long fileId, String token){
        String url = "https://api.curseforge.com/v1/mods/" + modId + "/files/" + fileId;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("x-api-key", token);
        ResponseEntity<CurseforgeFile> response = restTemplate.exchange(url, HttpMethod.GET,  new HttpEntity<>(headers), CurseforgeFile.class);
        return response.getBody();
    }
}
