package com.abc.n.m;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ObjectMapperConfiguration {


    @Primary
    @Bean("objectMapper")
    public ObjectMapper getObjectMapper() {
        return (new ObjectMapper()).setSerializationInclusion(Include.NON_NULL).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

}