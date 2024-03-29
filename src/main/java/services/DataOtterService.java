package services;

import com.fasterxml.jackson.databind.ObjectMapper;
import data.api.monitor.Monitor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.LinkedList;

@Service
public class DataOtterService {

    public LinkedList<Monitor> getMonitorStatus(){
        LinkedList<Monitor> monitors = new LinkedList<>();
        String URL = "http://54.211.139.84:8080/monitors";
        // String URL = "http://localhost:8080/monitors";
        RestTemplate restTemplate = new RestTemplate();
        String response;
        try {
            response = restTemplate.getForObject(new URI(URL), String.class);
            ObjectMapper om = new ObjectMapper();
            JSONArray jsonArray = new JSONArray(response);
            for(int i = 0; i < jsonArray.length(); i++){
                JSONObject monitor = jsonArray.getJSONObject(i);
                monitors.add(om.readValue(monitor.toString(), Monitor.class));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return monitors;
    }
}
