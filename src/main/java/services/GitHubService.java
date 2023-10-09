package services;

import com.fasterxml.jackson.databind.ObjectMapper;
import data.api.github.Label;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;

public abstract class GitHubService {

    public static LinkedList<Label> getIssueLabels(String url){
        LinkedList<Label> labels = new LinkedList<>();
        RestTemplate restTemplate = new RestTemplate();
        String response;
        try {
            response = restTemplate.getForObject(new URI(url), String.class);
            ObjectMapper om = new ObjectMapper();
            labels = new LinkedList<>(List.of(om.readValue(response, data.api.github.Label[].class)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return labels;
    }
}
