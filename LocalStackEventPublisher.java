package com.abc.n.m.component.testcontainer;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;

import java.time.Duration;
import java.util.Map;

import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@AllArgsConstructor
@Slf4j
public class LocalStackEventPublisher {
    private final String queueURL;
    private final QueueMessagingTemplate queueMessagingTemplate;


    @SneakyThrows
    public void publishEvent(@NonNull final SQSEvent msg) {

        queueMessagingTemplate.convertAndSend(queueURL, msg, Map.of("Content-Type", "application/json"));

        await().pollDelay(Duration.ofSeconds(1)).until(() -> true);

    }


}
