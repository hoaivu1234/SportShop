package com.sport.ecommerce.infrastructure.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

/**
 * Thin wrapper around the Cloudinary SDK.
 *
 * <p>Upload strategies supported:
 * <ul>
 *   <li><b>uploadFromUrl</b>  — Cloudinary fetches the image from a remote URL.
 *       No local download needed; best for seeding from external sources.</li>
 *   <li><b>uploadBytes</b>    — Upload raw bytes (e.g. multipart file from a form).</li>
 * </ul>
 *
 * <p>Uses <em>signed upload</em> via the configured api-key + api-secret —
 * more secure than unsigned preset-based uploads and suitable for server-side use.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    /**
     * Tells Cloudinary to fetch and store the image at {@code sourceUrl}.
     * The network request is made from Cloudinary's servers, not from this JVM.
     *
     * @param sourceUrl publicly accessible image URL
     * @param folder    Cloudinary folder path (e.g. "sport-shop/products")
     * @return secure HTTPS URL of the uploaded image
     */
    public String uploadFromUrl(String sourceUrl, String folder) {
        log.debug("Uploading to Cloudinary from URL: {}", sourceUrl);
        try {
            @SuppressWarnings("rawtypes")
            Map result = cloudinary.uploader().upload(
                    sourceUrl,
                    ObjectUtils.asMap(
                            "resource_type", "image",
                            "folder", folder
                    )
            );
            String secureUrl = (String) result.get("secure_url");
            log.info("Cloudinary upload OK → {}", secureUrl);
            return secureUrl;
        } catch (IOException e) {
            log.error("Cloudinary upload failed for URL {}: {}", sourceUrl, e.getMessage());
            throw new CloudinaryUploadException("Failed to upload image from URL: " + sourceUrl, e);
        }
    }

    /**
     * Uploads raw image bytes to Cloudinary.
     *
     * @param imageBytes image content
     * @param folder     Cloudinary folder path
     * @return secure HTTPS URL of the uploaded image
     */
    public String uploadBytes(byte[] imageBytes, String folder) {
        log.debug("Uploading {} bytes to Cloudinary", imageBytes.length);
        try {
            @SuppressWarnings("rawtypes")
            Map result = cloudinary.uploader().upload(
                    imageBytes,
                    ObjectUtils.asMap(
                            "resource_type", "image",
                            "folder", folder
                    )
            );
            String secureUrl = (String) result.get("secure_url");
            log.info("Cloudinary upload OK → {}", secureUrl);
            return secureUrl;
        } catch (IOException e) {
            log.error("Cloudinary upload failed: {}", e.getMessage());
            throw new CloudinaryUploadException("Failed to upload image bytes", e);
        }
    }

    // ── Typed exception ────────────────────────────────────────────────────────

    public static class CloudinaryUploadException extends RuntimeException {
        public CloudinaryUploadException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
