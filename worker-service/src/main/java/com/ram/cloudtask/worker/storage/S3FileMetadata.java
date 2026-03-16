package com.ram.cloudtask.worker.storage;
public record S3FileMetadata(
        long size,
        String contentType
) {}