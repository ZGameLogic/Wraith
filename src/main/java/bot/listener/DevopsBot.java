package bot.listener;

import application.App;
import com.zgamelogic.jda.AdvancedListenerAdapter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedList;

import static com.zgamelogic.jda.Annotations.*;

@Slf4j
@RestController
public class DevopsBot extends AdvancedListenerAdapter {

    @Autowired
    public DevopsBot(){
//        getCommands().add();
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