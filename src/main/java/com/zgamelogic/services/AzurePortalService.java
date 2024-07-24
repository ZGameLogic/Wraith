package com.zgamelogic.services;

import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.zgamelogic.dataotter.DataOtterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AzurePortalService {
    private final SecretClient secretClient;
    private final DataOtterService dataOtterService;

    @Autowired
    public AzurePortalService(
            @Value("${keyvault.url}") String keyVaultUrl,
            @Value("${keyvault.client.id}") String keyVaultClientId,
            @Value("${keyvault.client.secret}") String keyVaultClientSecret,
            @Value("${keyvault.tenant.id}") String keyVaultTenantId, DataOtterService dataOtterService
    ) {
        this.dataOtterService = dataOtterService;
        log.info("Initializing Azure Service");
        secretClient = new SecretClientBuilder()
                .vaultUrl(keyVaultUrl)
                .credential(new ClientSecretCredentialBuilder()
                        .clientId(keyVaultClientId)
                        .clientSecret(keyVaultClientSecret)
                        .tenantId(keyVaultTenantId)
                        .build()
                ).buildClient();
        log.info("Azure service initialized");
    }

    @Bean
    public SecretClient client(){
        return secretClient;
    }
}
