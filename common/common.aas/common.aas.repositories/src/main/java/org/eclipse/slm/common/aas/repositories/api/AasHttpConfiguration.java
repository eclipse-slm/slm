package org.eclipse.slm.common.aas.repositories.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.eclipse.digitaltwin.basyx.http.Aas4JHTTPSerializationExtension;
import org.eclipse.digitaltwin.basyx.http.SerializationExtension;
import org.eclipse.digitaltwin.basyx.submodelrepository.http.SubmodelRepositoryHTTPSerializationExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.util.List;

@Configuration
public class AasHttpConfiguration {

    private final static Logger LOG = LoggerFactory.getLogger(AasHttpConfiguration.class);

    @Bean
    public SubmodelRepositoryHTTPSerializationExtension submodelRepositoryHTTPSerializationExtension() {
        return new SubmodelRepositoryHTTPSerializationExtension();
    }

    @Bean
    public Aas4JHTTPSerializationExtension aas4JHTTPSerializationExtension() {
        return new Aas4JHTTPSerializationExtension();
    }

    @Bean
    public Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder(List<SerializationExtension> serializationExtensions) {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder().serializationInclusion(JsonInclude.Include.NON_NULL);

        for (SerializationExtension serializationExtension : serializationExtensions) {
            serializationExtension.extend(builder);
        }

        return builder;
    }

}
