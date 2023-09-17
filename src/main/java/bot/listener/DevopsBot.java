package bot.listener;

import com.zgamelogic.jda.AdvancedListenerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class DevopsBot extends AdvancedListenerAdapter {

    @Autowired
    public DevopsBot(){

    }

    @GetMapping("health")
    private String healthCheck(){
        return "Healthy";
    }

    @PostMapping("github")
    private void github(@RequestBody String body){
        log.info(body);
    }
}