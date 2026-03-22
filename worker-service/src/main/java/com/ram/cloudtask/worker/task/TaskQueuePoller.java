package com.ram.cloudtask.worker.task;

import com.ram.cloudtask.worker.config.AwsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.List;
import java.util.UUID;

@Service
public class TaskQueuePoller {

    private static final Logger log = LoggerFactory.getLogger(TaskQueuePoller.class);

    private final SqsClient sqsClient;
    private final AwsProperties awsProperties;
    private final TaskWorkerService taskWorkerService;

    public TaskQueuePoller(SqsClient sqsClient,
                           AwsProperties awsProperties,
                           TaskWorkerService taskWorkerService) {
        this.sqsClient = sqsClient;
        this.awsProperties = awsProperties;
        this.taskWorkerService = taskWorkerService;
    }

    @Scheduled(fixedDelay = 5000)
    public void pollQueue() {
        ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                .queueUrl(awsProperties.getSqs().getQueueUrl())
                .maxNumberOfMessages(5)
                .waitTimeSeconds(5)
                .attributeNamesWithStrings("ApproximateReceiveCount")
                .build();

        List<Message> messages = sqsClient.receiveMessage(request).messages();

        if (messages.isEmpty()) {
            log.info("No messages in queue");
            return;
        }

        for (Message message : messages) {
            processMessage(message);
        }
    }

    private void processMessage(Message message) {
        String messageBody = message.body();
        String receiveCount = message.attributes().get("ApproximateReceiveCount");
        log.info("Received message from SQS: {}, receiveCount={}", messageBody, receiveCount);

        try {
            UUID taskId = UUID.fromString(messageBody);

            taskWorkerService.processTask(taskId);

            DeleteMessageRequest deleteRequest = DeleteMessageRequest.builder()
                    .queueUrl(awsProperties.getSqs().getQueueUrl())
                    .receiptHandle(message.receiptHandle())
                    .build();

            sqsClient.deleteMessage(deleteRequest);

            log.info("Successfully processed task {} and deleted SQS message", taskId);

        } catch (Exception e) {
            log.error("Failed to process SQS message. body={}, receiptHandle={}", messageBody, message.receiptHandle(), e);
        }
    }
}