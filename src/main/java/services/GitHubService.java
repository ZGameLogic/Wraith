package services;

import data.api.github.Label;
import data.api.github.LabelsPayload;
import data.api.github.WorkflowRun;
import data.api.github.responses.FilePayload;
import data.api.github.responses.Tree;
import data.api.github.responses.TreePayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@Service
public class GitHubService {

    @Value("${github.token}")
    private String githubToken;

    @Value("${github.admin.token}")
    private String githubAdminToken;

    public List<Tree> getPropertiesFileList(){
        String url = "https://api.github.com/repos/ZGameLogic/properties/git/trees/master?recursive=true";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + githubAdminToken);
        try {
            TreePayload response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), TreePayload.class).getBody();
            return response.getTree().stream().filter(item -> item.getPath().contains(".properties")).toList();
        } catch (Exception e){
            log.error("Unable to get list of properties files", e);
        }
        return List.of();
    }

    public String getPropertiesFileContent(String filePath){
        String url = "https://api.github.com/repos/ZGameLogic/properties/contents/" + filePath;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + githubAdminToken);
        try {
            FilePayload response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), FilePayload.class).getBody();
            return response.decodeContent();
        } catch (Exception e){
            log.error("Unable to get content for file {}", filePath, e);
        }
        return "";
    }

    public void editIssueLabels(String repository, long issueNumber, LabelsPayload payload){
        String url = "https://api.github.com/repos/ZGameLogic/" + repository + "/issues/" + issueNumber + "/labels";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + githubToken);
        try {
            HttpEntity<LabelsPayload> requestEntity = new HttpEntity<>(payload, headers);
            restTemplate.put(url, requestEntity);
        } catch (Exception e){
            log.error("Canned edit issue labels", e);
        }
    }

    public LinkedList<Label> getIssueLabels(String url){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + githubAdminToken);
        try {
            ResponseEntity<Label[]> response = restTemplate.exchange(url, HttpMethod.GET,  new HttpEntity<>(headers), Label[].class);
            return new LinkedList<>(Arrays.asList(response.getBody()));
        } catch (Exception e) {
            log.error("Error getting issue labels", e);
        }
        return new LinkedList<>();
    }

    public WorkflowRun getWorkflowRun(String url){
        url += "/jobs";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + githubAdminToken);
        try {
            ResponseEntity<WorkflowRun> response = restTemplate.exchange(url, HttpMethod.GET,  new HttpEntity<>(headers), WorkflowRun.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Error getting workflow run", e);
        }
        return null;
    }
}
