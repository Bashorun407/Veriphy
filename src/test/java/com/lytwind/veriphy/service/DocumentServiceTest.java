package com.lytwind.veriphy.service;

import com.lytwind.veriphy.entity.DocumentRecord;
import com.lytwind.veriphy.entity.DocumentStatus;
import com.lytwind.veriphy.repository.DocumentRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DocumentServiceTest {
    // @Mock creates a "fake" repository so we don't touch the real PostgreSQL database
    @Mock
    private DocumentRecordRepository repository;

    // @InjectMocks puts our fake repository into the real DocumentService
    @InjectMocks
    private DocumentService documentService;

    @Test
    void revokeDocument_Success_ChangesStatusToRevoked() {
        // --- GIVEN (The Setup) ---
        UUID testId = UUID.randomUUID();
        DocumentRecord mockRecord = DocumentRecord.builder()
                .id(testId)
                .status(DocumentStatus.VALID) // Starts as valid
                .build();

        // Tell our fake database what to return when findById is called
        when(repository.findById(testId)).thenReturn(Optional.of(mockRecord));
        // Tell our fake database to just return the record when save is called
        when(repository.save(any(DocumentRecord.class))).thenReturn(mockRecord);

        // --- WHEN (The Action) ---
        DocumentRecord result = documentService.revokeDocument(testId);

        // --- THEN (The Verification) ---
        // Prove the status actually changed
        assertEquals(DocumentStatus.REVOKED, result.getStatus());
        // Prove that the repository's save method was called exactly 1 time
        verify(repository, times(1)).save(mockRecord);
    }

    @Test
    void revokeDocument_ThrowsException_IfAlreadyRevoked() {
        // --- GIVEN ---
        UUID testId = UUID.randomUUID();
        DocumentRecord mockRecord = DocumentRecord.builder()
                .id(testId)
                .status(DocumentStatus.REVOKED) // Already revoked!
                .build();

        when(repository.findById(testId)).thenReturn(Optional.of(mockRecord));

        // --- WHEN & THEN ---
        // Prove that calling revoke on an already revoked document throws a RuntimeException
        Exception exception = assertThrows(RuntimeException.class, () -> {
            documentService.revokeDocument(testId);
        });

        // Prove the error message is correct
        assertTrue(exception.getMessage().contains("already been revoked"));
        // Prove that the database never tried to save anything (preventing redundant database hits)
        verify(repository, never()).save(any());
    }
}
