package services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class KubernetesService {
    private final String BASE_URL = "https://zgamelogic.com:25000/api/v1";

    @Value("${kubernetes.token}")
    private String kubernetesToken;

    public String getDefaultPods(){
        String url = BASE_URL + "/namespaces/default/pods";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + kubernetesToken);
        try {
            return restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class).getBody();
        } catch (Exception e){
            log.error("Unable to get kubernetes pods", e);
        }
        return null;
    }
}
