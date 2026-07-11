package com.sribalajiads.media_app.controller;

import com.sribalajiads.media_app.dto.BulkImageUploadResult;
import com.sribalajiads.media_app.dto.BulkPptRequest;
import com.sribalajiads.media_app.dto.PptRequest;
import com.sribalajiads.media_app.model.Media;
import com.sribalajiads.media_app.service.MediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@RestController
@RequestMapping("/api/media")
public class MediaController {

    private final MediaService mediaService;

    @Autowired
    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @PostMapping(consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Media> addMedia(
            @RequestParam("belongsTo") String belongsTo,
            @RequestParam("mediaCode") String mediaCode,
            @RequestParam("location") String location,
            @RequestParam("city") String city,
            @RequestParam("specifications") String specifications,
            @RequestParam("illumination") String illumination,
            @RequestParam("mediaType") String mediaType,
            @RequestParam(name = "trafficView", required = false) String trafficView,
            @RequestParam(name = "coordinates", required = false) String coordinates,
            @RequestPart("image") MultipartFile imageFile,
            @RequestParam(name = "locationUrl", required = false) String locationUrl) {

        Media createdMedia = mediaService.createMedia(belongsTo, mediaCode, location, city, specifications, illumination, mediaType, imageFile, trafficView, locationUrl, coordinates);
        return new ResponseEntity<>(createdMedia, HttpStatus.CREATED);
    }
@PostMapping
public ResponseEntity<Media> createMedia(...) throws IOException {
    Media media = mediaService.createMedia(...);
    return ResponseEntity.ok(media);
}
    @GetMapping
    public ResponseEntity<Page<Media>> getMedia(
            @RequestParam(name = "company", required = false) String company,
            @RequestParam(name = "mediaType", required = false) String mediaType,
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Media> mediaPage = mediaService.getMedia(company, mediaType, query, pageable);
        return ResponseEntity.ok(mediaPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Media> getMediaById(@PathVariable Long id) {
        Media media = mediaService.getMediaById(id);
        return ResponseEntity.ok(media);
    }

    @PostMapping("/generate-ppt")
    public ResponseEntity<InputStreamResource> generatePpt(@RequestBody PptRequest request) throws IOException {
        ByteArrayInputStream pptInputStream = mediaService.generatePresentationForMedia(request.getMediaIds(), request.getCompanyName());

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=presentation.pptx");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(pptInputStream));
    }

    @PostMapping("/generate-ppt-bulk")
    public ResponseEntity<InputStreamResource> generateBulkPpt(@RequestBody BulkPptRequest request) throws IOException {
        ByteArrayInputStream bis = mediaService.generateBulkPresentation(request.getCodes(), request.getCompanyName());
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Bulk_Proposal.pptx");
        return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_OCTET_STREAM).body(new InputStreamResource(bis));
    }

    @PostMapping("/generate-pdf")
    public ResponseEntity<InputStreamResource> generatePdf(@RequestBody PptRequest request) throws IOException {
        ByteArrayInputStream pdfInputStream = mediaService.generatePdfForMedia(request.getMediaIds(), request.getCompanyName());

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=presentation.pdf");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(pdfInputStream));
    }

    @PostMapping("/generate-pdf-bulk")
    public ResponseEntity<InputStreamResource> generateBulkPdf(@RequestBody BulkPptRequest request) throws IOException {
        ByteArrayInputStream bis = mediaService.generateBulkPdf(request.getCodes(), request.getCompanyName());
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Bulk_Proposal.pdf");
        return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF).body(new InputStreamResource(bis));
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Media> updateMedia(
            @PathVariable Long id,
            @RequestParam("belongsTo") String belongsTo,
            @RequestParam("mediaCode") String mediaCode,
            @RequestParam("location") String location,
            @RequestParam("city") String city,
            @RequestParam("specifications") String specifications,
            @RequestParam("illumination") String illumination,
            @RequestParam("mediaType") String mediaType,
            @RequestParam(name = "trafficView", required = false) String trafficView,
            @RequestParam(name = "coordinates", required = false) String coordinates,
            @RequestPart(name = "image", required = false) MultipartFile imageFile,
            @RequestParam(name = "locationUrl", required = false) String locationUrl) {

        Media updatedMedia = mediaService.updateMedia(id, belongsTo, mediaCode, location, city, specifications, illumination, mediaType, trafficView, imageFile, locationUrl, coordinates);
        return ResponseEntity.ok(updatedMedia);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMedia(@PathVariable Long id) {
        mediaService.deleteMedia(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/bulk-image-upload", consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BulkImageUploadResult> bulkImageUpload(@RequestParam("zip") MultipartFile zipFile) throws IOException {
        return ResponseEntity.ok(mediaService.processBulkImageZip(zipFile));
    }
}
