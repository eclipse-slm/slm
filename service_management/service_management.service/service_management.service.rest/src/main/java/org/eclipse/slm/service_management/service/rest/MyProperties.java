package org.eclipse.slm.service_management.service.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@RefreshScope
@Configuration
@ConfigurationProperties("my")
public class MyProperties {

//    @Value("${my.prop}")
    private String prop;

    public String getProp() {
        return prop;
    }

    public void setProp(String prop) {
        this.prop = prop;
    }


    @PostConstruct
    public void postConstruct() {
        // to validate if properties are loaded
        System.out.println("** prop: " + prop);
    }

    // standard getter, setter
}
