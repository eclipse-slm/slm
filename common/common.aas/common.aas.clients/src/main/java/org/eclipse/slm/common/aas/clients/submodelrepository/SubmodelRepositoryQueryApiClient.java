package org.eclipse.slm.common.aas.clients.submodelrepository;

import feign.Body;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import org.eclipse.slm.common.aas.model.submodelrepository.responses.SubmodelQueryResult;
import org.springframework.http.MediaType;

import java.util.Map;

/// API Client for the Submodel Repository Query according to API [V3.1.1_SSP-005](https://app.swaggerhub.com/apis/Plattform_i40/SubmodelRepositoryServiceSpecification/V3.1.1_SSP-005)
public interface SubmodelRepositoryQueryApiClient {

    @RequestLine("POST /query/submodels?limit={limit}&cursor={cursor}")
    @Headers("Content-Type: " + MediaType.TEXT_PLAIN_VALUE)
    @Body("{query}")
    SubmodelQueryResult querySubmodel(@Param("limit") int limit, @Param("cursor") String cursor, Map<String, Object> query);

}
