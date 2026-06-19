package org.eclipse.slm.resource_management.common.access;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
public class AccessControlService {

    private final AccessControlPolicyRepository policyRepository;

    public AccessControlService(AccessControlPolicyRepository policyRepository) {
        this.policyRepository = policyRepository;
    }

    public AccessControlPolicy createSingleObjectPolicy(
            String name, String subjectGroup,
            AccessControlObjectType objectType, UUID objectId) {
        AccessControlPolicy policy = new AccessControlPolicy();
        policy.setName(name);
        policy.getSubjects().add(subjectGroup);
        policy.getObjects().add(new AccessControlObjectRef(objectType, objectId));
        return policyRepository.save(policy);
    }

    public boolean hasAccess(
            AccessControlObjectType objectType, UUID objectId, UserContext userContext) {
        if (userContext.isAdmin()) {
            return true;
        }
        if (userContext.getGroups().isEmpty()) {
            return false;
        }
        return policyRepository.countAccessGranting(
                userContext.getGroups(), objectType, objectId) > 0;
    }

    /**
     * @return Optional.empty() for admins (= no filtering / all objects accessible),
     *         otherwise the set of object ids the user's groups may access.
     */
    public Optional<Set<UUID>> getAccessibleObjectIds(
            UserContext userContext, AccessControlObjectType objectType) {
        if (userContext.isAdmin()) {
            return Optional.empty();
        }
        if (userContext.getGroups().isEmpty()) {
            return Optional.of(Set.of());
        }
        return Optional.of(policyRepository.findAccessibleObjectIds(
                userContext.getGroups(), objectType));
    }

    @Transactional
    public void removeObjectFromAllPolicies(
            AccessControlObjectType objectType, UUID objectId) {
        var policies = policyRepository.findByObject(objectType, objectId);
        for (AccessControlPolicy policy : policies) {
            policy.getObjects().removeIf(
                ref -> ref.getObjectType() == objectType && ref.getObjectId().equals(objectId));
            if (policy.getObjects().isEmpty()) {
                policyRepository.delete(policy);
            } else {
                policyRepository.save(policy);
            }
        }
    }
}
