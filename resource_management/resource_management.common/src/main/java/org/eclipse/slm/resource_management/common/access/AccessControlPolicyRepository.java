package org.eclipse.slm.resource_management.common.access;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface AccessControlPolicyRepository extends JpaRepository<AccessControlPolicy, UUID> {

    @Query("SELECT o.objectId FROM AccessControlPolicy p " +
           "JOIN p.objects o JOIN p.subjects s " +
           "WHERE s IN :groups AND o.objectType = :objectType")
    Set<UUID> findAccessibleObjectIds(
            @Param("groups") Set<String> groups,
            @Param("objectType") AccessControlObjectType objectType);

    @Query("SELECT p FROM AccessControlPolicy p JOIN p.objects o " +
           "WHERE o.objectType = :objectType AND o.objectId = :objectId")
    List<AccessControlPolicy> findByObject(
            @Param("objectType") AccessControlObjectType objectType,
            @Param("objectId") UUID objectId);

    @Query("SELECT DISTINCT s FROM AccessControlPolicy p JOIN p.subjects s JOIN p.objects o " +
           "WHERE o.objectType = :objectType AND o.objectId = :objectId")
    Set<String> findSubjectsByObject(
            @Param("objectType") AccessControlObjectType objectType,
            @Param("objectId") UUID objectId);

    @Query("SELECT COUNT(p) FROM AccessControlPolicy p " +
           "JOIN p.objects o JOIN p.subjects s " +
           "WHERE s IN :groups AND o.objectType = :objectType AND o.objectId = :objectId")
    long countAccessGranting(
            @Param("groups") Set<String> groups,
            @Param("objectType") AccessControlObjectType objectType,
            @Param("objectId") UUID objectId);
}
