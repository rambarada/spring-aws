package com.ram.cloudtask.worker.task;

public enum TaskStatus {
    PENDING,
    PROCESSING,
    RETRYING,
    DONE,
    FAILED
}