package com.abc.n.m.component.testcontainer;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.aws.core.env.ResourceIdResolver;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS;

@Configuration
@Slf4j
public class LocalStackConfiguration {


    @Bean("localStacklLambdaContainer")
    @SneakyThrows
    public LocalStackLambdaTestContainer localStackLambdaContainer() {

        final LocalStackLambdaTestContainer lambdaContainer = LocalStackLambdaTestContainer.getInstance();
        lambdaContainer.start();


        return lambdaContainer;
    }

    @Bean
    @Primary
    public AmazonSQSAsync amazonSQSAsync(final LocalStackLambdaTestContainer localStacklLambdaContainer) {

        return AmazonSQSAsyncClientBuilder.standard()
                .withCredentials(localStacklLambdaContainer.getDefaultCredentialsProvider())
                .withEndpointConfiguration(localStacklLambdaContainer.getEndpointConfiguration(SQS))
                .build();

    }

    @Bean
    public QueueMessagingTemplate queueMessagingTemplate(
            AmazonSQSAsync amazonSQSAsync, ObjectMapper objectMapper) {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setSerializedPayloadClass(String.class);
        converter.setObjectMapper(objectMapper);
        return new QueueMessagingTemplate(amazonSQSAsync, (ResourceIdResolver) null, converter);
    }

    @Bean
    public LocalStackEventPublisher localStackEventPublisher(final QueueMessagingTemplate queueMessagingTemplate) {

        return new LocalStackEventPublisher("n-queue", queueMessagingTemplate);
    }

}
