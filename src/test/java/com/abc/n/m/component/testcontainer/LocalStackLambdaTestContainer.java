package com.abc.n.m.component.testcontainer;

import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.localstack.LocalStackContainer;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.Architecture;
import software.amazon.awssdk.services.lambda.model.CreateFunctionRequest;
import software.amazon.awssdk.services.lambda.model.CreateFunctionResponse;
import software.amazon.awssdk.services.lambda.model.Environment;
import software.amazon.awssdk.services.lambda.model.FunctionCode;
import software.amazon.awssdk.services.lambda.model.PackageType;
import software.amazon.awssdk.services.lambda.model.Runtime;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

@Slf4j
public class LocalStackLambdaTestContainer extends LocalStackContainer {

    private static final String IMAGE_VERSION = "1.4.0";

    // private static final String IMAGE_VERSION = "0.14.0";

    private static final String localstackNetworkAlias = "localstack";


    private static AwsCredentialsProvider credentialsProvider;

    private static LocalStackLambdaTestContainer container;

    public LocalStackLambdaTestContainer() {
        super(IMAGE_VERSION);

        //super(DockerImageName.parse("localstack/localstack").asCompatibleSubstituteFor("localstack/localstack"));
    }

    public static LocalStackLambdaTestContainer getInstance() {

        if (container == null) {
            container =
                    (LocalStackLambdaTestContainer) new LocalStackLambdaTestContainer().withServices(Service.LAMBDA, Service.SQS)
                            .withEnv("DEFAULT_REGION", Region.US_EAST_1.toString())
                            .withNetwork(Network.SHARED)

                            .withNetworkAliases(localstackNetworkAlias)

                            .withEnv("LAMBDA_DOCKER_NETWORK", ((Network.NetworkImpl) Network.SHARED).getName())
                            .withEnv("SQS_DOCKER_NETWORK", ((Network.NetworkImpl) Network.SHARED).getName())
                            .withFileSystemBind(
                                    new File("target/").getPath(), "/opt/code/localstack/target", BindMode.READ_ONLY)
                            .withReuse(false);

            log.info("container starting");

            credentialsProvider = StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(container.getAccessKey(), container.getSecretKey()));


        }
        return container;
    }

    @Override
    public void start() {
        log.info("container starting");
        super.start();

        log.info("Lambda localstack test container started");

    }

    @Override
    public void stop() {
        super.stop();
    }

    public void cretaeSQSLambdaTrigger() throws Exception {

        container.execInContainer(
                "awslocal", "lambda", "create-event-source-mapping",
                "--endpoint-url", "http://localhost:4566",
                "--function-name", "note-queue-lambda",
                "--batch-size", "1",
                "--event-source-arn", "arn:aws:sqs:us-east-1:000000000000:note-queue");
    }


    public void snedQueueMsg() throws Exception {
        container.execInContainer(
                "awslocal", "--endpoint-url", "http://localhost:4566",
                "sqs", "send-message", "--queue-url=http://localhost:4566/000000000000/note-queue",
                "--message-body", "file:///src/test/resources/sqs/sqs_event_body.json");
    }

    public void createQueueListenerLambdaFunction() throws FileNotFoundException {
        final Map<String, String> variables =
                Map.of(
                        "AWS_ACCESS_KEY_ID",
                        container.getAccessKey(),
                        "AWS_SECRET_ACCESS_KEY",
                        container.getSecretKey(),
                        "LAMBDA_ENDPOINT_OVERRIDE",
                        "http://" + localstackNetworkAlias + ":" + 4566);


        final CreateFunctionRequest request =
                CreateFunctionRequest.builder()
                        .functionName("n-queue-lambda")
                        .runtime(Runtime.JAVA11)
                        .handler("com.abc.n.m.infrastructure.lambda.SqsHandler")
                        .role("arn:aws:iam::000000000000:role/lambda")
                        .packageType(PackageType.ZIP)
                        .code(
                                FunctionCode.builder()
                                        .zipFile(
                                                SdkBytes.fromInputStream(
                                                        new FileInputStream("target/spring-lambda-localstack-0.0.1-SNAPSHOT-aws" +
                                                                ".jar")))
                                        .build())
                        .environment(Environment.builder().variables(variables).build())
                        .timeout(900)
                        .architectures(Architecture.X86_64)
                        .memorySize(1024)
                        .build();

        final CreateFunctionResponse response = getLambdaClient().createFunction(request);
        log.info("Created  lambda response: {}", response.toString());
    }


    public SqsClient getSQSClient() {
        return SqsClient.builder()
                .region(Region.of(LocalStackLambdaTestContainer.getInstance().getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(LocalStackLambdaTestContainer.getInstance().getAccessKey(), LocalStackLambdaTestContainer.getInstance().getSecretKey())))
                .endpointOverride(LocalStackLambdaTestContainer.getInstance().getEndpointOverride(Service.SQS))
                .build();
    }

    public LambdaClient getLambdaClient() {
        return LambdaClient.builder()
                .region(Region.of(LocalStackLambdaTestContainer.getInstance().getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(LocalStackLambdaTestContainer.getInstance().getAccessKey(), LocalStackLambdaTestContainer.getInstance().getSecretKey())))
                .endpointOverride(LocalStackLambdaTestContainer.getInstance().getEndpointOverride(LocalStackContainer.Service.LAMBDA))
                .build();
    }

}
