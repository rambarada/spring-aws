package com.ram.cloudtask.api.task;

import com.ram.cloudtask.api.storage.S3StorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskQueuePublisher taskQueuePublisher;
    private final S3StorageService s3StorageService;

    public TaskService(TaskRepository taskRepository,
                       TaskQueuePublisher taskQueuePublisher,
                       S3StorageService s3StorageService) {
        this.taskRepository = taskRepository;
        this.taskQueuePublisher = taskQueuePublisher;
        this.s3StorageService = s3StorageService;
    }

    public TaskResponse createTask(MultipartFile file) {

        String s3Key = s3StorageService.uploadFile(file);

        TaskEntity task = new TaskEntity();
        task.setOriginalFileName(file.getOriginalFilename());
        task.setS3Key(s3Key);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        task.setStatus(TaskStatus.PENDING);

        TaskEntity savedTask = taskRepository.save(task);

        taskQueuePublisher.publishTask(savedTask.getId().toString());

        return mapToResponse(savedTask);
    }

    public List<TaskResponse> getAllTasks() {
        return taskRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public TaskResponse getTaskById(UUID taskId) {
        TaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        return mapToResponse(task);
    }

    private TaskResponse mapToResponse(TaskEntity task) {
        return new TaskResponse(
                task.getId(),
                task.getOriginalFileName(),
                task.getS3Key(),
                task.getStatus(),
                task.getCreatedAt(),
                task.getUpdatedAt(),
                task.getProcessedAt()
        );
    }
}