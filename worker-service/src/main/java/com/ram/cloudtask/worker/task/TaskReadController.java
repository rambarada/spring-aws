package com.ram.cloudtask.worker.task;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TaskReadController {

    private final TaskRepository taskRepository;

    public TaskReadController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @GetMapping("/worker/tasks/count")
    public long countTasks() {
        return taskRepository.count();
    }
}