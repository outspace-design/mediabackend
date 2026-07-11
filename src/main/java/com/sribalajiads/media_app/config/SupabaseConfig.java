package com.sribalajiads.media_app.config;
// import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
public class SupabaseConfig {

    @Value("${supabase.endpoint}")
    private String endpoint;

    @Value("${supabase.region}")
    private String region;

    @Value("${supabase.access-key}")
    private String accessKey;

    @Value("${supabase.secret-key}")
    private String secretKey;

@Bean(name = "supabaseClient")
public S3Client supabaseClient() {
    return S3Client.builder()
            .httpClientBuilder(UrlConnectionHttpClient.builder())
            .endpointOverride(URI.create(endpoint))
            .region(Region.of(region))
            .credentialsProvider(
                    StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(accessKey, secretKey)))
            .forcePathStyle(true)
            .build();
}
}
