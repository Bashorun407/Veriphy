package com.lytwind.veriphy.controller;

import com.lytwind.veriphy.entity.DocumentRecord;
import com.lytwind.veriphy.entity.DocumentStatus;
import com.lytwind.veriphy.entity.DocumentType;
import com.lytwind.veriphy.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

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
}
