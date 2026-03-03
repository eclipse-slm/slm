package org.eclipse.slm.common.credentials.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface CredentialLinkJpaRepository extends JpaRepository<CredentialEntityLink, Long> {

    List<CredentialEntityLink> findByCredentialId(UUID credentialId);

    List<CredentialEntityLink> findByEntityIdAndEntityType(String entityId, String entityType);

    List<CredentialEntityLink> findByEntityIdAndCredentialId(String entityId, UUID credentialId);

    List<CredentialEntityLink> findByEntityIdAndEntityTypeAndCredentialId(String entityId, String entityType, UUID credentialId);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    void deleteByCredentialId(UUID credentialId);

}