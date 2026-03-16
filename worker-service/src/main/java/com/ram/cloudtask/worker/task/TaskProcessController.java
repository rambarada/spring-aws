package com.ram.cloudtask.worker.task;

import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/worker/tasks")
public class TaskProcessController {

    private final TaskWorkerService taskWorkerService;

    public TaskProcessController(TaskWorkerService taskWorkerService) {
        this.taskWorkerService = taskWorkerService;
    }

    @PostMapping("/{taskId}/process")
    public TaskEntity processTask(@PathVariable UUID taskId) {
        return taskWorkerService.processTask(taskId);
    }
}