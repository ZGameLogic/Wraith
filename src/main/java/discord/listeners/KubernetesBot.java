package discord.listeners;

import com.zgamelogic.annotations.DiscordController;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import services.KubernetesService;

@DiscordController
public class KubernetesBot {

    private final KubernetesService kubernetesService;

    @Autowired
    public KubernetesBot(KubernetesService kubernetesService) {
        this.kubernetesService = kubernetesService;
    }

    @PostConstruct
    private void post(){
    }
}
