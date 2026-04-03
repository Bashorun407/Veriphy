package com.lytwind.veriphy.controller;

import com.lytwind.veriphy.entity.DocumentRecord;
import com.lytwind.veriphy.entity.DocumentStatus;
import com.lytwind.veriphy.entity.DocumentType;
import com.lytwind.veriphy.security.JwtService;
import com.lytwind.veriphy.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final JwtService jwtService;

    // Add this inside DocumentController.java
    @PostMapping("/verify")
    public ResponseEntity<?> verifyUploadedDocument(@RequestParam("file") MultipartFile file) {
        try {
            DocumentRecord record = documentService.verifyDocument(file);

            // If we get here, the document is mathematically proven to be authentic!
            if (record.getStatus() == DocumentStatus.VALID) {
                return ResponseEntity.ok("VERIFICATION SUCCESS: This is an authentic " + record.getDocumentType()
                        + " issued to " + record.getRecipientId());
            } else {
                return ResponseEntity.status(403).body(" VERIFICATION FAILED: This document exists but its status is: "
                        + record.getStatus());
            }

        } catch (Exception e) {
            return ResponseEntity.status(404).body("Exception caught! " + e.getMessage());
        }
    }

    // A simple GET request to trigger document creation for testing
    @GetMapping("/issue")
    public ResponseEntity<?> issueTestDocument(
            @RequestParam String studentId,
            @RequestParam DocumentType type) {
        try {
            DocumentRecord record = documentService.issueDocument(studentId, type);
            return ResponseEntity.ok(record);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    // Add this inside DocumentController.java
    @PatchMapping("/{id}/revoke")
    public ResponseEntity<?> revokeDocument(@PathVariable UUID id) {
        try {
            DocumentRecord record = documentService.revokeDocument(id);
            return ResponseEntity.ok(" SUCCESS: Document " + id + " has been successfully REVOKED.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // Add this to DocumentController.java

    // Endpoint 1: Admin generates the expiring link
    @GetMapping("/{id}/download-link")
    public ResponseEntity<?> generateDownloadLink(@PathVariable UUID id) {
        try {
            // Generate a 24-hour token for this specific document
            String downloadToken = jwtService.generateDownloadToken(id.toString());

            // Construct the final URL to give to the student
            String downloadUrl = "http://localhost:8080/api/v1/documents/download?token=" + downloadToken;

            return ResponseEntity.ok("Secure Download Link (Valid for 24h): " + downloadUrl);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // Endpoint 2: The student clicks the link and downloads the file
    @GetMapping("/download")
    public ResponseEntity<?> downloadDocument(@RequestParam("token") String token) {
        try {
            // The extractUsername method will throw an exception if the token is expired or tampered with!
            String documentIdString = jwtService.extractUsername(token);
            UUID documentId = UUID.fromString(documentIdString);

            Resource resource = documentService.getDocumentAsResource(documentId);

            // Tell the browser to download the file instead of just displaying text
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(403).body("Download Failed: Link is invalid or has expired.");
        }
    }
}
