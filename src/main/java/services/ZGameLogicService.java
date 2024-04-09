package services;

import data.api.zGameLogic.SOTData;
import data.discord.SeaOfThievesEventData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class ZGameLogicService {
    @Value("${zgamelogic.api-key}")
    private String API_KEY;

    public boolean postSeoOfThievesData(SeaOfThievesEventData data){
        String url = "https://zgamelogic.com/api/sot";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("api-key", API_KEY);
        try {
            HttpEntity<SOTData> requestEntity = new HttpEntity<>(new SOTData(data), headers);
            restTemplate.postForObject(url, requestEntity, String.class);
            return true;
        } catch(Exception e){
            log.error("Unable to post SOT data", e);
            return false;
        }
    }
}
