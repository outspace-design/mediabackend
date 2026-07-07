package com.sribalajiads.media_app.service;

import com.sribalajiads.media_app.model.Media;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class PdfGenerationService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private static final String NO_LOGO_OPTION = "NO_LOGO";

    // --- LAYOUT CONSTANTS (Widescreen 16:9 Page: 792 x 612 points = 11" x 8.5") ---
    private static final float PAGE_WIDTH = 792;
    private static final float PAGE_HEIGHT = 540;

    // Layout Coordinates
    private static final float LOGO_X = 20;
    private static final float LOGO_Y = PAGE_HEIGHT - 70;
    private static final float LOGO_MAX_W = 180;
    private static final float LOGO_MAX_H = 60;

    private static final float LOC_X = 220;
    private static final float LOC_Y = PAGE_HEIGHT - 50;
    private static final float LOC_W = PAGE_WIDTH - 240;
    private static final float LOC_H = 30;

    private static final float MAIN_IMG_X = 20;
    private static final float MAIN_IMG_Y = PAGE_HEIGHT - 480;
    private static final float MAIN_IMG_W = PAGE_WIDTH - 40;
    private static final float MAIN_IMG_H = 400;

    private static final float FOOTER_Y = 20;
    private static final float FOOTER_H = 40;

    public ByteArrayInputStream generatePdf(List<Media> mediaList, String companyName) throws IOException {

        try (PDDocument document = new PDDocument()) {
            boolean isNoLogo = NO_LOGO_OPTION.equalsIgnoreCase(companyName);

            // ============================================================
            // 1. WELCOME PAGE
            // ============================================================
            if (companyName != null && !companyName.isBlank() && !isNoLogo) {
                addFullScreenPage(document, companyName + "_WELCOME");
            }

            // ============================================================
            // 2. MEDIA PAGES
            // ============================================================
            for (Media media : mediaList) {
                PDPage page = new PDPage(new PDRectangle(PAGE_WIDTH, PAGE_HEIGHT));
                document.addPage(page);

                try (PDPageContentStream cs = new PDPageContentStream(document, page)) {

                    // --- A. COMPANY LOGO ---
                    if (!isNoLogo) {
                        String mediaOwner = media.getBelongsTo().name();
                        File logoFile = findFileByBaseName(uploadDir, mediaOwner + "LOGO");
                        drawImage(document, cs, logoFile, LOGO_X, LOGO_Y, LOGO_MAX_W, LOGO_MAX_H);
                    }

                    // --- B. LOCATION TEXT ---
                    String locationText = media.getLocation() != null ? media.getLocation().toUpperCase() : "";
                    cs.beginText();
                    cs.setFont(PDType1Font.HELVETICA_BOLD, 14);
                    cs.setNonStrokingColor(new Color(237, 28, 36));
                    float textWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(locationText) / 1000 * 14;
                    cs.newLineAtOffset(PAGE_WIDTH - 20 - textWidth, LOC_Y);
                    cs.showText(locationText);
                    cs.endText();

                    // --- C. MAIN MEDIA IMAGE (SMART LOOKUP) ---
                    String dbFilename = media.getImagePath();
                    String baseName = getBaseName(dbFilename);
                    File imageFile = findFileByBaseName(uploadDir, baseName);
                    drawImage(document, cs, imageFile, MAIN_IMG_X, MAIN_IMG_Y, MAIN_IMG_W, MAIN_IMG_H);

                    // --- D. DETAILS ---
                    StringBuilder details = new StringBuilder();
                    if (media.getMediaCode() != null) details.append(media.getMediaCode());
                    if (media.getMediaType() != null) details.append(" | ").append(media.getMediaType());
                    if (media.getSpecifications() != null) details.append(" | ").append(media.getSpecifications());
                    if (media.getIllumination() != null && !media.getIllumination().isBlank()) details.append(" | ").append(media.getIllumination());
                    if (media.getTrafficView() != null && !media.getTrafficView().isBlank()) details.append(" | ").append(media.getTrafficView());
                    if (media.getCity() != null) details.append(" | ").append(media.getCity());
                    if (media.getCoordinates() != null && !media.getCoordinates().isBlank()) details.append(" | ").append(media.getCoordinates());

                    String detailsText = details.toString().toUpperCase();

                    cs.beginText();
                    cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
                    cs.setNonStrokingColor(new Color(192, 0, 0));
                    cs.newLineAtOffset(20, FOOTER_Y + 10);
                    cs.showText(detailsText);
                    cs.endText();

                    // --- E. MAP LINK ---
                    String locationUrl = media.getLocationUrl();
                    if (locationUrl != null && !locationUrl.isBlank()) {
                        cs.beginText();
                        cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
                        cs.setNonStrokingColor(Color.BLUE);
                        String linkText = "\uD83D\uDDFA Street View";
                        float linkWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(linkText) / 1000 * 12;
                        cs.newLineAtOffset(PAGE_WIDTH - 20 - linkWidth, FOOTER_Y + 10);
                        cs.showText(linkText);
                        cs.endText();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // ============================================================
            // 3. THANK YOU PAGE
            // ============================================================
            if (companyName != null && !companyName.isBlank() && !isNoLogo) {
                addFullScreenPage(document, companyName + "_THANKYOU");
            }

            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                document.save(out);
                return new ByteArrayInputStream(out.toByteArray());
            }
        }
    }

    private void addFullScreenPage(PDDocument document, String baseFileName) {
        File imageFile = findFileByBaseName(uploadDir, baseFileName);
        if (imageFile != null && imageFile.exists()) {
            try {
                PDPage page = new PDPage(new PDRectangle(PAGE_WIDTH, PAGE_HEIGHT));
                document.addPage(page);
                try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                    drawImage(document, cs, imageFile, 0, 0, PAGE_WIDTH, PAGE_HEIGHT);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void drawImage(PDDocument document, PDPageContentStream cs, File imageFile,
                           float x, float y, float boxW, float boxH) {
        if (imageFile != null && imageFile.exists()) {
            try {
                byte[] imgBytes = convertToImageBytes(imageFile);
                if (imgBytes != null && imgBytes.length > 0) {
                    // Write to temp file since PDImageXObject needs a file or input stream
                    java.io.File tempFile = java.io.File.createTempFile("pdfbox_", ".png");
                    try {
                        java.io.FileOutputStream fos = new java.io.FileOutputStream(tempFile);
                        fos.write(imgBytes);
                        fos.close();

                        PDImageXObject pdImage = PDImageXObject.createFromFile(tempFile.getAbsolutePath(), document);

                        // Calculate aspect-ratio-preserving dimensions
                        float imgW = pdImage.getWidth();
                        float imgH = pdImage.getHeight();
                        float ratio = imgW / imgH;
                        float newW = boxW;
                        float newH = newW / ratio;
                        if (newH > boxH) {
                            newH = boxH;
                            newW = newH * ratio;
                        }
                        // Center in the box
                        float offsetX = x + (boxW - newW) / 2;
                        // PDF coordinates: y=0 is bottom, so we need to calculate from bottom
                        float offsetY = y + (boxH - newH) / 2;

                        cs.drawImage(pdImage, offsetX, offsetY, newW, newH);
                    } finally {
                        tempFile.delete();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private File findFileByBaseName(String dirPath, String baseName) {
        Path dir = Paths.get(dirPath);
        if (!Files.exists(dir) || !Files.isDirectory(dir)) return null;
        String cleanedBaseName = baseName.trim();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, cleanedBaseName + ".*")) {
            for (Path entry : stream) {
                return entry.toFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private byte[] convertToImageBytes(File file) throws IOException {
        String extension = getFileExtension(file.getName()).toLowerCase();
        if ("webp".equals(extension)) {
            try (InputStream is = new FileInputStream(file)) {
                BufferedImage image = ImageIO.read(is);
                if (image == null) throw new IOException("Cannot read WebP");
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    ImageIO.write(image, "png", baos);
                    return baos.toByteArray();
                }
            }
        } else {
            try (InputStream is = new FileInputStream(file)) {
                return is.readAllBytes();
            }
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null) return "";
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }

    private String getBaseName(String fileName) {
        if (fileName == null) return "";
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
    }
}
