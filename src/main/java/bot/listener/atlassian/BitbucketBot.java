package bot.listener.atlassian;

import com.zgamelogic.AdvancedListenerAdapter;
import org.json.JSONObject;

public class BitbucketBot extends AdvancedListenerAdapter {

    public void handleWebhook(JSONObject body){
        System.out.println(body);
    }
}
