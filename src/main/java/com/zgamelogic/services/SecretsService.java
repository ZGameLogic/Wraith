package com.zgamelogic.services;

import com.zgamelogic.data.database.sercets.Secret;
import com.zgamelogic.data.database.sercets.SecretRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class SecretsService {
    private final SecretRepository secretRepository;

    public List<Secret> listAvailableSecrets(Collection<Long> access) {
        return secretRepository.findAllByAccessIn(access);
    }

    public Optional<Secret> getSecretValue(UUID uuid, Collection<Long> access){
        return secretRepository.findByIdAndAccessIn(uuid, access);
    }

    public void setSecretValue(String name, String value, long access){
        Secret secret = new Secret(name, value, access);
        secretRepository.save(secret);
    }
}
