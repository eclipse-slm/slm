package org.eclipse.slm.common.awx.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;


public class AwxLoginCache {

    @Bean
    @Scope("singleton")
    public AwxLoginCache awxLoginCacheSingleton(){
        return new AwxLoginCache();
    }

    final int MAX_ENTRIES = 50;

    Map<String, String> cache = Collections.synchronizedMap(new LinkedHashMap<>(MAX_ENTRIES + 1, .75F, true) {
        // This method is called just after a new entry has been added
        public boolean removeEldestEntry(Map.Entry eldest) {
            return size() > MAX_ENTRIES;
        }
    });

    public void put(String token, String sessionIdAndCsrfToken){
        this.cache.put(token, sessionIdAndCsrfToken);
    }

    public String get(String token){
        return this.cache.get(token);
    }

    public boolean has(String token){
        return this.cache.containsKey(token);
    }

}
