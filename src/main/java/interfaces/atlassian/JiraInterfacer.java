package interfaces.atlassian;

import application.App;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public abstract class JiraInterfacer {

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
}
