package com.ram.cloudtask.worker.storage;

import com.ram.cloudtask.worker.config.AwsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;

@Service
public class S3WorkerService {

    private static final Logger log = LoggerFactory.getLogger(S3WorkerService.class);

    private final S3Client s3Client;
    private final AwsProperties awsProperties;

    public S3WorkerService(S3Client s3Client, AwsProperties awsProperties) {
        this.s3Client = s3Client;
        this.awsProperties = awsProperties;
    }

    public S3FileMetadata inspectFile(String s3Key) {

        HeadObjectRequest request = HeadObjectRequest.builder()
                .bucket(awsProperties.getS3().getBucketName())
                .key(s3Key)
                .build();

        HeadObjectResponse response = s3Client.headObject(request);

        return new S3FileMetadata(
                response.contentLength(),
                response.contentType()
        );
    }
}