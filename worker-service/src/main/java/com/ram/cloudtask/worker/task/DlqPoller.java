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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class DlqPoller {

    private static final Logger log = LoggerFactory.getLogger(DlqPoller.class);

    private final SqsClient sqsClient;
    private final TaskRepository taskRepository;
    private final AwsProperties awsProperties;

    public DlqPoller(SqsClient sqsClient, TaskRepository taskRepository,AwsProperties awsProperties) {
        this.sqsClient = sqsClient;
        this.taskRepository = taskRepository;
        this.awsProperties = awsProperties;
    }

    @Scheduled(fixedDelay = 10000)
    public void pollDlq() {
        ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                .queueUrl(this.awsProperties.getSqs().getDlqUrl())
                .maxNumberOfMessages(5)
                .waitTimeSeconds(5)
                .build();

        List<Message> messages = sqsClient.receiveMessage(request).messages();

        for (Message message : messages) {
            processDlqMessage(message);
        }
    }

    private void processDlqMessage(Message message) {
        String messageBody = message.body();
        log.info("Received DLQ message. body={}", messageBody);

        try {
            UUID taskId = UUID.fromString(messageBody);

            TaskEntity task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new RuntimeException("Task not found for DLQ message: " + taskId));

            task.setStatus(TaskStatus.FAILED);
            task.setUpdatedAt(LocalDateTime.now());
            taskRepository.save(task);

            sqsClient.deleteMessage(DeleteMessageRequest.builder()
                    .queueUrl(this.awsProperties.getSqs().getDlqUrl())
                    .receiptHandle(message.receiptHandle())
                    .build());

            log.info("Marked task {} as FAILED from DLQ and deleted DLQ message", taskId);

        } catch (Exception e) {
            log.error("Failed to process DLQ message. body={}", messageBody, e);
        }
    }
}