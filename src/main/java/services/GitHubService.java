package services;

import data.api.github.Label;
import data.api.github.WorkflowRun;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.LinkedList;

@Slf4j
public abstract class GitHubService {

    public static LinkedList<Label> getIssueLabels(String url, String githubToken){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + githubToken);
        try {
            ResponseEntity<Label[]> response = restTemplate.exchange(url, HttpMethod.GET,  new HttpEntity<>(headers), Label[].class);
            return new LinkedList<>(Arrays.asList(response.getBody()));
        } catch (Exception e) {
            log.error("Error getting issue labels", e);
        }
        return new LinkedList<>();
    }

    public static WorkflowRun getWorkflowRun(String url, String githubToken){
        url += "/jobs";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + githubToken);
        try {
            ResponseEntity<WorkflowRun> response = restTemplate.exchange(url, HttpMethod.GET,  new HttpEntity<>(headers), WorkflowRun.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Error getting workflow run", e);
        }
        return null;
    }
}
