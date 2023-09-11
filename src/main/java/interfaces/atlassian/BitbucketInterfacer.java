package interfaces.atlassian;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Slf4j
public abstract class BitbucketInterfacer {

    public static JSONObject createPullRequest(String bitbucketURL, String bitbucketPAT, String project, String repo, String from, String to){
        String url = bitbucketURL + "rest/api/latest/projects/" + project + "/repos/" + repo + "/pull-requests";
        RestTemplate restTemplate = new RestTemplate();
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject body = new JSONObject();
        try {
            body.put("title", "Automatic pull request from discord");
            body.put("description", "This pull request was created by the wraith discord bot.");
            body.put("state", "OPEN");
            body.put("open", true);
            body.put("closed", false);
            body.put("locked", false);
            JSONObject repository = new JSONObject();
            repository.put("slug", repo);
            repository.put("project", new JSONObject("{\"key\": \"" + project + "\"}"));
            JSONObject fromRef = new JSONObject();
            fromRef.put("id", "refs/heads/" + from);
            fromRef.put("repository", repository);
            body.put("fromRef", fromRef);
            JSONObject toRef = new JSONObject();
            toRef.put("id", "refs/heads/" + to);
            toRef.put("repository", repository);
            body.put("toRef", toRef);
        } catch (JSONException e){
            log.error("Error when creating JSON object", e);
        }
        headers.add("Authorization", "Bearer " + bitbucketPAT);
        HttpEntity<String> request = new HttpEntity<>(body.toString(), headers);
        try {
            return new JSONObject(restTemplate.postForObject(url, request, String.class));
        } catch (JSONException e) {
            log.error("Error when posting webhook request", e);
            return null;
        }
    }

    public static JSONObject mergePullRequest(String bitbucketURL, String bitbucketPAT, String baseURL, int webhookPort, String project, String repo, long prId, long version){
        String url = bitbucketURL + "rest/api/latest/projects/" + project + "/repos/" + repo + "/pull-requests/" + prId + "/merge";
        RestTemplate restTemplate = new RestTemplate();
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject body = new JSONObject();
        String baseUrl = baseURL + ":" + webhookPort + "/webhooks/bitbucket";
        try {
            body.put("version", version);
        } catch (JSONException e){
            log.error("Error when creating JSON object", e);
        }
        headers.add("Authorization", "Bearer " + bitbucketPAT);
        HttpEntity<String> request = new HttpEntity<>(body.toString(), headers);
        try {
            return new JSONObject(restTemplate.postForObject(url, request, String.class));
        } catch (JSONException e) {
            log.error("Error when posting webhook request", e);
            return null;
        }
    }

    public static JSONObject getBitbucketRepository(String bitbucketURL, String bitbucketPAT, String project, String repo){
        String url = bitbucketURL + "rest/api/latest/projects/" + project + "/repos/" + repo;
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpget = new HttpGet(url);
        httpget.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + bitbucketPAT);
        try {
            HttpResponse httpresponse = httpclient.execute(httpget);
            if (httpresponse.getStatusLine().getStatusCode() != 200) return null;
            BufferedReader in = new BufferedReader(new InputStreamReader(httpresponse.getEntity().getContent()));
            return new JSONObject(in.readLine());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static JSONObject getBitbucketCommit(String bitbucketURL, String bitbucketPAT, String project, String repo, String commit){
        String url = bitbucketURL + "rest/api/latest/projects/" + project + "/repos/" + repo + "/commits/" + commit;
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpget = new HttpGet(url);
        httpget.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + bitbucketPAT);
        try {
            HttpResponse httpresponse = httpclient.execute(httpget);
            if (httpresponse.getStatusLine().getStatusCode() != 200) return null;
            BufferedReader in = new BufferedReader(new InputStreamReader(httpresponse.getEntity().getContent()));
            return new JSONObject(in.readLine());
        } catch (IOException | JSONException e) {
            log.error("Error when getting bitbucket commit", e);
            return null;
        }
    }

    public static JSONObject getBitbucketBranches(String bitbucketURL, String bitbucketPAT, String project, String repo){
        String url = bitbucketURL + "rest/api/latest/projects/" + project + "/repos/" + repo + "/branches";
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpget = new HttpGet(url);
        httpget.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + bitbucketPAT);
        try {
            HttpResponse httpresponse = httpclient.execute(httpget);
            if (httpresponse.getStatusLine().getStatusCode() != 200) return null;
            BufferedReader in = new BufferedReader(new InputStreamReader(httpresponse.getEntity().getContent()));
            return new JSONObject(in.readLine());
        } catch (IOException | JSONException e) {
            log.error("Error when getting bitbucket branches", e);
            return null;
        }
    }

    public static JSONObject getBitbucketProjectRepos(String bitbucketURL, String bitbucketPAT, String project){
        String url = bitbucketURL + "rest/api/latest/projects/" + project + "/repos/";
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpget = new HttpGet(url);
        httpget.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + bitbucketPAT);
        try {
            HttpResponse httpresponse = httpclient.execute(httpget);
            if (httpresponse.getStatusLine().getStatusCode() != 200) return null;
            BufferedReader in = new BufferedReader(new InputStreamReader(httpresponse.getEntity().getContent()));
            return new JSONObject(in.readLine());
        } catch (IOException | JSONException e) {
            log.error("Error when getting bitbucket branches", e);
            return null;
        }
    }

    public static JSONObject createWebhook(String bitbucketURL, String bitbucketPAT, String baseURL, int webhookPort, String project, String repo){
        String url = bitbucketURL + "rest/api/latest/projects/" + project + "/repos/" + repo + "/webhooks";
        RestTemplate restTemplate = new RestTemplate();
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject body = new JSONObject();
        String baseUrl = baseURL + ":" + webhookPort + "/webhooks/bitbucket";
        try {
            body.put("name", "Wraith webhook");
            body.put("url", baseUrl);
            body.put("active", true);
            JSONArray events = new JSONArray();
            events.put("repo:refs_changed");
            events.put("pr:opened");
            events.put("pr:merged");
            body.put("events", events);
        } catch (JSONException e){
            log.error("Error when creating JSON object", e);
        }

        headers.add("Authorization", "Bearer " + bitbucketPAT);
        HttpEntity<String> request = new HttpEntity<>(body.toString(), headers);
        try {
            return new JSONObject(restTemplate.postForObject(url, request, String.class));
        } catch (JSONException e) {
            log.error("Error when posting webhook request", e);
            return null;
        }
    }
}
