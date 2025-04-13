package com.zgamelogic.services;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.dns.DnsZoneManager;
import com.azure.resourcemanager.dns.models.DnsZone;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.identity.ClientSecretCredentialBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AzurePortalService {
    private final DnsZoneManager dnsZoneManager;
    private final SecretClient secretClient;

    @Autowired
    public AzurePortalService(
            @Value("${keyvault.url}") String keyVaultUrl,
            @Value("${keyvault.client.id}") String keyVaultClientId,
            @Value("${keyvault.client.secret}") String keyVaultClientSecret,
            @Value("${keyvault.tenant.id}") String keyVaultTenantId
    ) {
        log.info("Initializing Azure Service");

        TokenCredential token = new ClientSecretCredentialBuilder()
                .clientId(keyVaultClientId)
                .clientSecret(keyVaultClientSecret)
                .tenantId(keyVaultTenantId)
                .build();

        secretClient = new SecretClientBuilder()
                .vaultUrl(keyVaultUrl)
                .credential(token)
                .buildClient();

        AzureProfile profile = new AzureProfile(keyVaultTenantId, "a9aca397-699d-4a31-9a43-eb239aa50d31", AzureEnvironment.AZURE);

        dnsZoneManager = DnsZoneManager.authenticate(token, profile);
        log.info("Azure service initialized");
    }

    public void addCnameRecord(String recordName) {
        String dnsZoneName = "zgamelogic.com";
        String resourceGroupName = "Azure_resource_group_1";
        int ttl = 3600;

        log.info("Adding CNAME record: {} -> {}", recordName, dnsZoneName);
        DnsZone dnsZone = dnsZoneManager.zones().getByResourceGroup(resourceGroupName, dnsZoneName);
        if (dnsZone == null) {
            throw new IllegalArgumentException("DNS Zone not found: " + dnsZoneName);
        }

        dnsZone.update()
                .defineCNameRecordSet(recordName)
                .withAlias(dnsZoneName)
                .withTimeToLive(ttl)
                .attach()
                .apply();

        log.info("CNAME record created: {}", recordName);
    }

    @Bean
    public SecretClient client(){
        return secretClient;
    }
}
