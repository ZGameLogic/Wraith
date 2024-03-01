package services;

import data.api.GPT.payloads.ImagePayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class GPTService {

    private final HttpHeaders headers;
    private final RestTemplate restTemplate;

    @Autowired
    public GPTService(@Value("${open.api.key}") String openApiKey){
        headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + openApiKey);
        restTemplate = new RestTemplate();
    }

    public void generateImage(String prompt){
        String url = "https://api.openai.com/v1/images/generations";
        try {
            HttpEntity<ImagePayload> requestEntity = new HttpEntity<>(new ImagePayload(prompt), headers);
            String resp = restTemplate.postForObject(url, requestEntity, String.class);
            System.out.println(resp);
        } catch(Exception e){
            log.error("Unable to create image", e);
        }
    }
}
