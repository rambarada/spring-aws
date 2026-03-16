package com.ram.cloudtask.api.task;

import com.ram.cloudtask.api.config.AwsProperties;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Service
public class TaskQueuePublisher {

    private final SqsClient sqsClient;
    private final AwsProperties awsProperties;

    public TaskQueuePublisher(SqsClient sqsClient, AwsProperties awsProperties) {
        this.sqsClient = sqsClient;
        this.awsProperties = awsProperties;
    }

    public void publishTask(String taskId) {

        SendMessageRequest request = SendMessageRequest.builder()
                .queueUrl(awsProperties.getSqs().getQueueUrl())
                .messageBody(taskId)
                .build();

        sqsClient.sendMessage(request);
    }
}