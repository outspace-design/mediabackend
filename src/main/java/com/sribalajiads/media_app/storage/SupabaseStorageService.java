package com.sribalajiads.media_app.storage;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class SupabaseStorageService {

    private static final String FOLDER = "media_app";

    private final S3Client supabaseClient;

    @Value("${supabase.bucket}")
    private String bucket;
    @Value("${supabase.public-url}")
    private String publicBaseUrl;

    public SupabaseStorageService(@Qualifier("supabaseClient") S3Client supabaseClient) {
        this.supabaseClient = supabaseClient;
    }

    public String upload(byte[] data, String mediaCode, String extension) {
        String key = FOLDER + "/" + mediaCode + "." + extension;
        try {
            supabaseClient.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType("image/" + (extension.equals("jpg") ? "jpeg" : extension))
                            .build(),
                    RequestBody.fromBytes(data)
            );
            return publicBaseUrl + "/" + key;
        } catch (Exception e) {
            // Never breaks main Cloudinary flow — Supabase is just an original-backup
            System.err.println(">>> Supabase upload failed for " + mediaCode + ": " + e.getMessage());
            return null;
        }
    }

    public void delete(String mediaCode, String extension) {
        try {
            String key = FOLDER + "/" + mediaCode + "." + extension;
            supabaseClient.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
        } catch (Exception e) {
            System.err.println(">>> Supabase delete failed: " + e.getMessage());
        }
    }
}
