package com.lytwind.veriphy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "document_records")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DocumentRecord implements Persistable<UUID> {

    @Id
    // removed @GeneratedValue to fully control the UUID
    private UUID id;

    @Column(nullable = false)
    private String recipientId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType documentType;

    @Column(nullable = false, unique = true, updatable = false)
    private String documentHash;

    @Column(nullable = false)
    private String storagePath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime issuedAt;

    @Column
    private LocalDateTime expiresAt;

    @Transient // Tells JPA not to save this boolean to the database
    @Builder.Default
    private boolean isNewRecord = true;

    @Override
    public boolean isNew() {
        return isNewRecord;
    }

    @PostPersist
    @PostLoad
    protected void markNotNew() {
        this.isNewRecord = false;
    }
    // ---------------

    @PrePersist
    protected void onCreate() {
        this.issuedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = DocumentStatus.VALID;
        }
    }
}