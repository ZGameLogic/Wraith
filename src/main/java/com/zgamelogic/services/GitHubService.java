package com.zgamelogic.services;

import com.zgamelogic.data.api.github.*;
import com.zgamelogic.data.api.github.payloads.*;
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
    private String githubBotToken;

    @Value("${github.admin.token}")
    private String githubAdminToken;

    public void closeIssue(String repository, long issue, String userToken){
        String url = String.format("https://api.github.com/repos/ZGameLogic/%s/issues/%d", repository, issue);
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + (userToken != null ? userToken : githubBotToken));
        try {
            HttpEntity<String> requestEntity = new HttpEntity<>("{\"state\":\"closed\"}", headers);
            restTemplate.patchForObject(url, requestEntity, String.class);
        } catch(Exception e){
            log.error("Unable to post comment", e);
        }
    }

    public Issue createIssue(String title, String desc, String repo, String userToken){
        String url = String.format("https://api.github.com/repos/ZGameLogic/%s/issues", repo);
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + (userToken != null ? userToken : githubBotToken));
        try {
            HttpEntity<IssueCreatePayload> requestEntity = new HttpEntity<>(new IssueCreatePayload(title, desc), headers);
            return restTemplate.postForObject(url, requestEntity, Issue.class);
        } catch(Exception e){
            log.error("Unable to create issue", e);
        }
        return null;
    }

    public void postIssueComment(String repository, long issue, String userToken, String message){
        String url = String.format("https://api.github.com/repos/ZGameLogic/%s/issues/%d/comments", repository, issue);
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + (userToken != null ? userToken : githubBotToken));
        try {
            HttpEntity<MessagePayload> requestEntity = new HttpEntity<>(new MessagePayload(message), headers);
            restTemplate.postForObject(url, requestEntity, String.class);
        } catch(Exception e){
            log.error("Unable to post comment", e);
        }
    }

    public User getGithubAuthenticatedUser(String userToken){
        String url = "https://api.github.com/user";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + userToken);
        try {
            return restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), User.class).getBody();
        } catch (Exception e){
            log.error("Unable to authenticate user", e);
        }
        return null;
    }

    public List<Tree> getPropertiesFileList(){
        String url = "https://api.github.com/repos/ZGameLogic/properties/git/trees/master?recursive=true";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + githubAdminToken);
        try {
            TreePayload response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), TreePayload.class).getBody();
            return response.getTree().stream().filter(item -> !item.getPath().contains(".github") && (item.getPath().contains(".properties") || item.getPath().contains(".yml"))).toList();
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
        headers.add("Authorization", "Bearer " + githubBotToken);
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
