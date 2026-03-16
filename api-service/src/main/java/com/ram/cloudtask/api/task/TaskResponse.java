package com.ram.cloudtask.api.task;

import java.time.LocalDateTime;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        String originalFileName,
        String s3Key,
        TaskStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime processedAt
) {
}