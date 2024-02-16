package services;

import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.security.keyvault.secrets.models.SecretProperties;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@Getter
@Slf4j
public class AzurePortalService {
    private final SecretClient secretClient;

    @Autowired
    public AzurePortalService(
            @Value("${keyvault.url}") String keyVaultUrl,
            @Value("${keyvault.client.id}") String keyVaultClientId,
            @Value("${keyvault.client.secret}") String keyVaultClientSecret,
            @Value("${keyvault.tenant.id}") String keyVaultTenantId
    ) {
        secretClient = new SecretClientBuilder()
                .vaultUrl(keyVaultUrl)
                .credential(new ClientSecretCredentialBuilder()
                        .clientId(keyVaultClientId)
                        .clientSecret(keyVaultClientSecret)
                        .tenantId(keyVaultTenantId)
                        .build()
                ).buildClient();
//        for (SecretProperties secretProperties : secretClient.listPropertiesOfSecrets()) {
//            System.out.println("Secret Name: " + secretProperties.getName());
//        }
    }

    @Bean
    public SecretClient client(){
        return secretClient;
    }
}
