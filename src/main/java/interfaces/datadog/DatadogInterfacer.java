package interfaces.datadog;

import application.App;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import data.api.datadog.Monitor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public abstract class DatadogInterfacer {

    public static Monitor[] getMonitors(){
        String baseUrl = App.config.getDatadogBaseUrl();

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = getHeaders();

        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(baseUrl + "/monitor", HttpMethod.GET, request, String.class);

        try {
            ObjectMapper om = new ObjectMapper();
            return om.readValue(response.getBody(), Monitor[].class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static HttpHeaders getHeaders(){
        String apiKey = App.config.getDatadogApiKey();
        String appKey = App.config.getDatadogAppKey();
        HttpHeaders headers = new HttpHeaders();
        headers.add("DD-API-KEY", apiKey);
        headers.add("DD-APPLICATION-KEY", appKey);
        headers.add("Accept", "application/json");
        return headers;
    }
}
