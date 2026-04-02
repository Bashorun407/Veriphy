package com.lytwind.veriphy.controller;

import com.lytwind.veriphy.entity.DocumentRecord;
import com.lytwind.veriphy.entity.DocumentType;
import com.lytwind.veriphy.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

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
