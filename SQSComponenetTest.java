package com.abc.n.m.component;

import com.abc.n.m.LocalStackApplication;
import com.abc.n.m.component.testcontainer.LocalStackEventPublisher;
import com.abc.n.m.component.testcontainer.LocalStackLambdaTestContainer;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.tests.annotations.Event;
import com.amazonaws.services.lambda.runtime.tests.annotations.Events;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.services.lambda.model.CreateEventSourceMappingRequest;
import software.amazon.awssdk.services.sqs.SqsClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = LocalStackApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Slf4j
@ActiveProfiles("test")
class SQSComponenetTest {

    @Autowired
    private ObjectMapper objectMapper;


    @Autowired
    private LocalStackEventPublisher localStackEventPublisher;

    private static final String queueUrl = "http://127.0.0.1:4566/000000000000/n-queue";

    private static final String queueARN = "arn:aws:sqs:us-east-1:000000000000:n-queue";

    @BeforeEach
    public void beforeEach() throws Exception {

        LocalStackLambdaTestContainer.getInstance().createQueueListenerLambdaFunction();

        LocalStackLambdaTestContainer.getInstance().execInContainer("awslocal", "sqs", "create-queue", "--queue-name", "n-queue");

        CreateEventSourceMappingRequest createEventSourceMappingRequest = CreateEventSourceMappingRequest
                .builder()
                .functionName("n-queue-lambda")
                .eventSourceArn(queueARN).build();

        LocalStackLambdaTestContainer.getInstance().getLambdaClient().createEventSourceMapping(createEventSourceMappingRequest);

/*
        LocalStackLambdaTestContainer.getInstance().execInContainer(
                "awslocal", "lambda", "create-event-source-mapping",
                "--endpoint-url", "http://localhost:4566",
                "--function-name", "n-queue-lambda",
                "--batch-size", "1",
                "--event-source-arn", queueARN);*/


        Thread.sleep(5000);
    }

    @ParameterizedTest
    @Events(events = {
            @Event("src/test/resources/sqs/sqs_event_body.json")},
            type = SQSEvent.class)
    void handleRequest_simple_success(SQSEvent event) throws Exception {

        log.info(" test sqs json is :-{}", objectMapper.writeValueAsString(event));


        assertNotNull(event);
        assertEquals(1, event.getRecords().size());

        final SqsClient sqsClient = LocalStackLambdaTestContainer.getInstance().getSQSClient();


        localStackEventPublisher.publishEvent(event);


        Thread.sleep(5000);

        log.info(" invoked lambda with logs: {}", LocalStackLambdaTestContainer.getInstance().getLogs());

        log.info("test");
    }

}
