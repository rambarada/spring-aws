package com.ram.cloudtask.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "aws")
public class AwsProperties {

    private String region;
    private SqsProperties sqs;
    private S3Properties s3;

    @Getter
    @Setter
    public static class SqsProperties {
        private String queueUrl;
        private String dlqUrl;
    }

    @Getter
    @Setter
    public static class S3Properties {
        private String bucketName;
    }
}