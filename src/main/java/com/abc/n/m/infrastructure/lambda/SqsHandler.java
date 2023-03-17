package com.abc.n.m.infrastructure.lambda;


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@NoArgsConstructor
public class SqsHandler implements RequestHandler<SQSEvent, String> {

    private ObjectMapper objectMapper =
            (new ObjectMapper()).setSerializationInclusion(JsonInclude.Include.NON_NULL).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Override
    public String handleRequest(SQSEvent sqsEvent, Context context) {

        log.info("sqsEvent:{}", sqsEvent);

        sqsEvent.getRecords().forEach(sqsMessage -> {
            log.info(" sqsMessage is :{}", sqsMessage);

            log.info(" sqsMessage body is :{}", sqsMessage.getBody());
            try {
                TestSQS test = objectMapper.readValue(sqsMessage.getBody(), TestSQS.class);

                log.info(" TestSQS is :{}", test);
            } catch (Exception e) {
                log.error("exception", e);
                throw new RuntimeException();
            }

        });

        return "OK";
    }
}
