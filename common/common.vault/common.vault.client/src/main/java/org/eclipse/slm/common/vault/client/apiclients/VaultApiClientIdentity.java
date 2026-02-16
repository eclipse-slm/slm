package org.eclipse.slm.common.vault.client.apiclients;

import feign.Body;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import org.eclipse.slm.common.vault.model.Group;
import org.eclipse.slm.common.vault.model.VaultApiResponse;
import org.eclipse.slm.common.vault.model.auth.JwtGroupAliasCreateRequest;

import java.util.List;
import java.util.Map;

public interface VaultApiClientIdentity {

    @RequestLine("GET /identity/group/name/{groupName}")
    VaultApiResponse<Group> getGroupByName(@Param("groupName") String groupName);

    @RequestLine("POST /identity/group/name/{groupName}")
    @Headers("Content-Type: application/json")
    @Body("{group}")
    void createOrUpdateGroupByName(@Param("groupName") String groupName, Group group);

    @RequestLine("DELETE /identity/group/name/{groupName}")
    void deleteGroupByName(@Param("groupName") String groupName);

    @RequestLine("GET /identity/group/name?list=true")
    @Headers("Content-Type: application/json")
    VaultApiResponse<Map<String, List<String>>> getAllGroupNames();

    @RequestLine("POST /identity/group-alias")
    @Headers({"Content-Type: application/json",})
    @Body("{request}")
    void createJwtGroupAlias(JwtGroupAliasCreateRequest request);

    @RequestLine("DELETE /identity/group-alias/id/{groupAliasId}")
    void deleteJwtGroupAliasById(@Param("groupAliasId") String groupAliasId);
}

