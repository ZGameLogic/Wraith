package interfaces.atlassian;

import application.App;
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

public abstract class JiraInterfacer {

    public static JSONObject sendCommentToIssue(String issueKey, String message, String username){
        String url = App.config.getJiraURL() + "rest/api/2/issue/" + issueKey + "/comment";
        RestTemplate restTemplate = new RestTemplate();
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject body = new JSONObject();
        try {
            body.put("body", "Discord user " + username + " added a comment\n" + message);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        headers.add("Authorization", "Bearer " + App.config.getJiraPAT());
        HttpEntity<String> request = new HttpEntity<>(body.toString(), headers);
        try {
            return new JSONObject(restTemplate.postForObject(url, request, String.class));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the details of a project by project key
     * @param projectKey Key of the project
     * @return JSONObject representation of the project
     */
    public static JSONObject getProject(String projectKey){
        String url = App.config.getJiraURL() + "rest/api/2/project/" + projectKey;
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpget = new HttpGet(url);
        httpget.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + App.config.getJiraPAT());
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

    /**
     * @param projectKey
     * @param title
     * @param description
     * @param username
     * @param userId
     * @return
     */
    public static JSONObject createBug(String projectKey, String title, String description, String username, String userId){
        String link = App.config.getJiraURL() + "/rest/api/2/issue";
        RestTemplate restTemplate = new RestTemplate();
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject body = new JSONObject();
        try {
            JSONObject fields = new JSONObject();
            fields.put("project", new JSONObject("{\"key\": \"" + projectKey + "\"}"));
            fields.put("summary", title);
            fields.put("description", description + "\n" +
                    "Discord username: " + username + "\n" +
                    "Discord user ID: " + userId);
            fields.put("assignee", new JSONObject("{\"name\":\"BShabowski\"}"));
            fields.put("issuetype", new JSONObject("{\"name\": \"Bug\"}"));
            JSONArray labels = new JSONArray();
            labels.put("Discord");
            fields.put("labels", labels);
            body.put("fields", fields);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        headers.add("Authorization", "Bearer " + App.config.getJiraPAT());
        HttpEntity<String> request = new HttpEntity<>(body.toString(), headers);

        try {
           return new JSONObject(restTemplate.postForObject(link, request, String.class));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param projectKey
     * @param title
     * @param description
     * @param username
     * @param userId
     * @return
     */
    public static JSONObject createTask(String projectKey, String title, String description, String[] inputLabels, String username, String userId){
        String link = App.config.getJiraURL() + "/rest/api/2/issue";
        RestTemplate restTemplate = new RestTemplate();
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject body = new JSONObject();
        try {
            JSONObject fields = new JSONObject();
            fields.put("project", new JSONObject("{\"key\": \"" + projectKey + "\"}"));
            fields.put("summary", title);
            fields.put("description", description + "\n" +
                    "Discord username: " + username + "\n" +
                    "Discord user ID: " + userId);
            fields.put("issuetype", new JSONObject("{\"name\": \"Task\"}"));
            JSONArray labels = new JSONArray();
            labels.put("Discord");
            for(String label: inputLabels) labels.put(label);
            fields.put("labels", labels);
            body.put("fields", fields);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        headers.add("Authorization", "Bearer " + App.config.getJiraPAT());
        HttpEntity<String> request = new HttpEntity<>(body.toString(), headers);

        try {
            return new JSONObject(restTemplate.postForObject(link, request, String.class));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
