package com.zgamelogic.secrets.database;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SecretRepository extends JpaRepository<Secret, UUID> {
    List<Secret> findAllByAccessIn(Collection<Long> accesses);
    Optional<Secret> findByIdAndAccessIn(UUID id, Collection<Long> accesses);
}
