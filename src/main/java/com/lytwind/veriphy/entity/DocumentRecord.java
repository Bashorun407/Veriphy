package com.lytwind.veriphy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "document_records")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DocumentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id; // Using UUID makes the document ID unguessable for security

    @Column(nullable = false)
    private String recipientId; // e.g., Student ID or Matrix Number

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType documentType;

    @Column(nullable = false, unique = true, updatable = false)
    private String documentHash; // SHA-256 hash to prove the document hasn't been tampered with

    @Column(nullable = false)
    private String storagePath; // Path where the PDF is saved (Local or S3)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime issuedAt;

    @Column
    private LocalDateTime expiresAt; // Nullable; some documents don't expire

    // This automatically sets the issue date right before it saves to the database
    @PrePersist
    protected void onCreate() {
        this.issuedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = DocumentStatus.VALID;
        }
    }
    
}
