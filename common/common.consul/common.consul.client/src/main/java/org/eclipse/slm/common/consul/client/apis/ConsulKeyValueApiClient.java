package org.eclipse.slm.common.consul.client.apis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orbitz.consul.ConsulException;
import com.orbitz.consul.option.DeleteOptions;
import org.eclipse.slm.common.consul.client.ConsulCredential;
import org.eclipse.slm.common.consul.client.utils.ConsulObjectMapper;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.common.consul.model.keyvalue.KeyValueReturnValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Component
public class ConsulKeyValueApiClient extends AbstractConsulApiClient {
    public final static Logger LOG = LoggerFactory.getLogger(ConsulKeyValueApiClient.class);
    private final ObjectMapper objectMapper;

    @Autowired
    public ConsulKeyValueApiClient(
            @Value("${consul.scheme}")       String consulScheme,
            @Value("${consul.host}")         String consulHost,
            @Value("${consul.port}")         int consulPort,
            @Value("${consul.acl-token}")    String consulToken,
            @Value("${consul.datacenter}")   String consulDatacenter,
            ObjectMapper objectMapper
    ) {
        super(consulScheme, consulHost, consulPort, consulToken, consulDatacenter);
        this.objectMapper = objectMapper;
    }

    public List<String> getKeys(ConsulCredential consulCredential, String baseKvPath) throws ConsulLoginFailedException {
        return this.getConsulClient(consulCredential).keyValueClient().getKeys(baseKvPath);
    }

    public void createKey(ConsulCredential consulCredential, String key, Object value) throws ConsulLoginFailedException {
        try {
            this.getConsulClient(consulCredential).keyValueClient().putValue(key, objectMapper.writeValueAsString(value));
        } catch (JsonProcessingException e) {
            return;
        }
        return;
    }

    private List<KeyValueReturnValue> doGetValueOfKeyCall(ConsulCredential consulCredential, String key) throws ConsulLoginFailedException {
        try {
            var value = this.getConsulClient(consulCredential).keyValueClient().getValue(key);
            if (value.isPresent()) {
                var response = new ArrayList<KeyValueReturnValue>();
                response.add(ConsulObjectMapper.map(value.get(), KeyValueReturnValue.class));
                return response;
            }

            return new ArrayList<>();
        }catch (ConsulException | HttpClientErrorException e){
            LOG.warn("No Key found at path '"+key+"'");
            LOG.warn(e.getMessage());
            return new ArrayList<>();
        }
    }

    public String getValueOfKey(ConsulCredential consulCredential, String key) throws ConsulLoginFailedException {
        List<KeyValueReturnValue> keyEntry = this.doGetValueOfKeyCall(consulCredential, key);

        if(keyEntry.size() == 0) {
            return null;
        }
        else {
            return new String(
                    Base64.getDecoder().decode(
                            keyEntry.get(0).getValue()
                    )
            );
        }
    }

    public <T> T getValueOfKey(ConsulCredential consulCredential, String key, TypeReference<T> typeReference) throws ConsulLoginFailedException, JsonProcessingException {
        String valueOfKeyAsString = this.getValueOfKey(consulCredential, key);

        if(valueOfKeyAsString == null)
            return null;

        return objectMapper.readValue(valueOfKeyAsString, typeReference);
    }

    public void deleteKey(ConsulCredential consulCredential, String key) throws ConsulLoginFailedException {
        this.getConsulClient(consulCredential).keyValueClient().deleteKey(key);
    }

    public void deleteKeyRecursive(ConsulCredential consulCredential, String key) throws ConsulLoginFailedException {
        this.getConsulClient(consulCredential).keyValueClient().deleteKey(key, DeleteOptions.RECURSE);
    }
}
