package com.ram.cloudtask.worker.task;

import com.ram.cloudtask.worker.storage.S3FileMetadata;
import com.ram.cloudtask.worker.storage.S3WorkerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TaskWorkerService {

    private final TaskRepository taskRepository;
    private final S3WorkerService s3WorkerService;

    public TaskWorkerService(TaskRepository taskRepository, S3WorkerService s3WorkerService) {
        this.taskRepository = taskRepository;
        this.s3WorkerService = s3WorkerService;
    }

    @Transactional public TaskEntity processTask(UUID taskId) {
        TaskEntity task = taskRepository.findById(taskId) .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));
        if (task.getStatus() == TaskStatus.DONE) {
            return task;
        }
        task.setStatus(TaskStatus.PROCESSING);
        task.setUpdatedAt(LocalDateTime.now());
        taskRepository.save(task);
        try { S3FileMetadata metadata = s3WorkerService.inspectFile(task.getS3Key());
            task.setFileSize(metadata.size());
            task.setContentType(metadata.contentType());
            task.setProcessedAt(LocalDateTime.now());
            Thread.sleep(3000);
            task.setStatus(TaskStatus.DONE);
            task.setUpdatedAt(LocalDateTime.now());
            return taskRepository.save(task);
        } catch (Exception e) {
            task.setStatus(TaskStatus.FAILED);
            task.setUpdatedAt(LocalDateTime.now());
            taskRepository.save(task);
            throw new RuntimeException("Failed to process task: " + taskId, e);
        }
    }
}