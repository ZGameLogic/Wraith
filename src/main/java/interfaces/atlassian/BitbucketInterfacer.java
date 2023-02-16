package interfaces.atlassian;

import application.App;
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

    public static JSONObject getBitbucketRepository(String project, String repo){
        String url = App.config.getBitbucketURL() + "rest/api/latest/projects/" + project + "/repos/" + repo;
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpget = new HttpGet(url);
        httpget.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + App.config.getBitbucketPAT());
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

    public static JSONObject getBitbucketCommit(String project, String repo, String commit){
        String url = App.config.getBitbucketURL() + "rest/api/latest/projects/" + project + "/repos/" + repo + "/commits/" + commit;
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpget = new HttpGet(url);
        httpget.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + App.config.getBitbucketPAT());
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

    public static JSONObject createWebhook(String project, String repo){
        String url = App.config.getBitbucketURL() + "rest/api/latest/projects/" + project + "/repos/" + repo + "/webhooks";
        RestTemplate restTemplate = new RestTemplate();
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject body = new JSONObject();
        String baseUrl = App.config.getBaseUrl() + ":" + App.config.getWebHookPort() + "/webhooks/bitbucket";
        try {
            body.put("name", "Wraith webhook");
            body.put("url", baseUrl);
            body.put("active", true);
            JSONArray events = new JSONArray();
            events.put("repo:refs_changed");
            events.put("repo:modified");
            events.put("pr:opened");
            events.put("pr:from_ref_updated"); // this ones for source branch
            events.put("pr:reviewer:approved");
            events.put("pr:merged");
            events.put("pr:declined");
            body.put("events", events);
        } catch (JSONException e){
            log.error("Error when creating JSON object", e);
        }

        headers.add("Authorization", "Bearer " + App.config.getBitbucketPAT());
        HttpEntity<String> request = new HttpEntity<>(body.toString(), headers);
        try {
            return new JSONObject(restTemplate.postForObject(url, request, String.class));
        } catch (JSONException e) {
            log.error("Error when posting webhook request", e);
            return null;
        }
    }
}
