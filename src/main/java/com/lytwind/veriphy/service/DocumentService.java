package com.lytwind.veriphy.service;

import com.lytwind.veriphy.entity.DocumentRecord;
import com.lytwind.veriphy.entity.DocumentStatus;
import com.lytwind.veriphy.entity.DocumentType;
import com.lytwind.veriphy.repository.DocumentRecordRepository;
import com.lytwind.veriphy.util.QrCodeGenerator;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRecordRepository repository;
    private final QrCodeGenerator qrCodeGenerator;
    private final TemplateEngine templateEngine; // Thymeleaf

    // This will save the PDFs to a folder named "secure-documents" in your project root
    @Value("${document.storage.path:./secure-documents}")
    private String storageDirectory;

    @PostConstruct
    public void init() throws Exception {
        // Ensure the storage directory exists when the app starts
        Files.createDirectories(Paths.get(storageDirectory));
    }

    public DocumentRecord verifyDocument(MultipartFile uploadedFile) throws Exception {
        // 1. Get the bytes of the uploaded file
        byte[] fileBytes = uploadedFile.getBytes();

        // 2. Generate the SHA-256 hash of the uploaded file
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(fileBytes);
        String uploadedHash = HexFormat.of().formatHex(hashBytes);

        // 3. Check if this hash exists in our database
        // If it was altered even slightly, the hash will change, and this will fail!
        return repository.findByDocumentHash(uploadedHash)
                .orElseThrow(() -> new RuntimeException("Verification Failed: Document is invalid, altered, or does not exist."));
    }

    public DocumentRecord issueDocument(String recipientId, DocumentType type) throws Exception {
        // 1. Pre-generate the Document UUID so we can put it in the QR code link
        UUID documentId = UUID.randomUUID();
        String verificationUrl = "http://localhost:8080/api/v1/verify/" + documentId;

        // 2. Generate the QR Code Base64 string
        String qrCodeBase64 = qrCodeGenerator.generateQrCodeBase64(verificationUrl);

        // 3. Prepare data for the Thymeleaf HTML template
        Context context = new Context();
        context.setVariable("recipientId", recipientId);
        context.setVariable("documentType", type.name());
        context.setVariable("issuedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        context.setVariable("qrCodeImage", qrCodeBase64);

        // 4. Render HTML
        String htmlContent = templateEngine.process("certificate", context);

        // 5. Convert HTML to PDF byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(htmlContent);
        renderer.layout();
        renderer.createPDF(outputStream);
        byte[] pdfBytes = outputStream.toByteArray();

        // 6. Generate SHA-256 Hash of the final PDF
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(pdfBytes);
        String documentHash = HexFormat.of().formatHex(hashBytes);

        // 7. Save the physical PDF file to the hard drive
        String fileName = documentId.toString() + ".pdf";
        Path filePath = Paths.get(storageDirectory, fileName);
        Files.write(filePath, pdfBytes);

        // 8. Save the record to the Database
        DocumentRecord record = DocumentRecord.builder()
                .id(documentId)
                .recipientId(recipientId)
                .documentType(type)
                .documentHash(documentHash)
                .storagePath(filePath.toString())
                .status(DocumentStatus.VALID)
                .build();

        return repository.save(record);
    }


    public DocumentRecord revokeDocument(UUID documentId) {
        // 1. Find the document in the database
        DocumentRecord record = repository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found with ID: " + documentId));

        // 2. Prevent redundant updates
        if (record.getStatus() == DocumentStatus.REVOKED) {
            throw new RuntimeException("This document has already been revoked.");
        }

        // 3. Change the status
        record.setStatus(DocumentStatus.REVOKED);

        // 4. Save the updated record.
        // Thanks to our Persistable interface setup, Spring Data JPA knows to execute an UPDATE query here!
        return repository.save(record);
    }
}
