package com.lytwind.veriphy.repository;

import com.lytwind.veriphy.entity.DocumentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentRecordRepository extends JpaRepository<DocumentRecord, UUID> {
    // We will use this in the final step to verify uploaded PDFs!
    Optional<DocumentRecord> findByDocumentHash(String documentHash);
}
