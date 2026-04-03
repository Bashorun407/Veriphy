# Project Name: Veriphy
## Author: Oluwaseyi Akinbobola
## Date: 2nd April 2026

Project Description:
This project implements a document issuance service that renders official PDFs (transcript/certificate) with QR code linking to a verification endpoint, stores files in object storage (locl/minio/S3-compatible), and issues time-limited download links. 
It further includes hashings/signature metadata and an admin dashboard API to revoke documents.

# Veriphy: Secure Document Issuance & Verification Microservice

##  Project Overview
Veriphy is a production-grade backend microservice designed to solve the problem of academic and official document forgery. It provides a complete workflow for generating physical, QR-coded PDF documents (like transcripts and certificates) and mathematically proving their authenticity through an automated upload-and-verify API.

This project was built as the final deliverable for the **SAFIntern Program**.

## Key Features
* **Automated PDF Generation:** Dynamically renders HTML templates using Thymeleaf and converts them into downloadable PDFs.
* **Embedded QR Codes:** Automatically generates and embeds Base64 encoded QR codes into documents for easy physical scanning.
* **Cryptographic Immutability:** Hashes the final physical PDF using the SHA-256 algorithm before saving, ensuring zero-tolerance for tampering.
* **Database Versioning:** Utilizes Flyway for strict, version-controlled database schema migrations.
* **Advanced JPA Lifecycle Management:** Implements the Spring Data `Persistable` interface to take manual control of UUID generation while preventing JPA `UPDATE/INSERT` conflicts.

## Security Architecture:
The core of this system relies on the **Avalanche Effect** of the SHA-256 hashing algorithm.

When a document is issued, the *exact bytes* of the PDF are hashed and stored in the database. When an employer or third party wants to verify the document, they upload the file to our endpoint. Our service re-hashes the uploaded file. Because of how SHA-256 works, if a student uses a PDF editor to change even a single pixel (e.g., changing a "B" grade to an "A"), the resulting hash changes entirely, and the verification will immediately fail. This guarantees mathematical proof of authenticity.

## Technology Stack
* **Language:** Java 17+
* **Framework:** Spring Boot 3.x
* **Database:** PostgreSQL
* **Migrations:** Flyway
* **Document Generation:** Thymeleaf, Flying Saucer (OpenPDF)
* **QR Codes:** Google ZXing
* **Build Tool:** Maven

---

## Setup & Installation

### 1. Prerequisites
* Java 17 or higher
* PostgreSQL installed and running on port `5432`
* Maven
* Postman (for API testing)

### 2. Database Configuration
Create a new database in PostgreSQL:
```sql
CREATE DATABASE document_db;
Update your application.properties (or application.yml) with your local database credentials:

Properties
spring.datasource.username=postgres
spring.datasource.password=your_password
3. Running the Application
Because this project uses Flyway, the database tables will be created automatically on startup.

Bash
mvn spring-boot:run
API Documentation
1. Issue a New Document
Generates a new secure PDF, saves it locally, and stores the hash in the database.

URL: /api/v1/documents/issue

Method: GET (Used for testing convenience)

Parameters:

studentId (String) - The ID of the recipient.

type (Enum) - e.g., CERTIFICATE, TRANSCRIPT

Example Request:http://localhost:8080/api/v1/documents/issue?studentId=MAT12345&type=CERTIFICATE

Example Success Response:

JSON
{
    "id": "a3930af0-2b45-4749-a786-e1291024c797",
    "recipientId": "MAT12345",
    "documentType": "CERTIFICATE",
    "documentHash": "cf1b80998c863675fbd91a0063912b5188...",
    "status": "VALID",
    "issuedAt": "2026-04-02T21:06:10.793"
}
2. Verify an Uploaded Document
Takes a physical PDF upload, re-hashes it, and verifies it against the secure database.

URL: /api/v1/documents/verify

Method: POST

Content-Type: multipart/form-data

Body Form-Data:

Key: file (Type: File)

Value: [Select the PDF file]

Example Success Response: VERIFICATION SUCCESS: This is an authentic CERTIFICATE issued to MAT12345

Example Failure Response (if document was altered): Verification Failed: Document is invalid, altered, or does not exist.

Postman Collection
A complete Postman collection is included in this repository to make evaluating the API instant and effortless.

Open Postman.

Click Import.

Select the Veriphy_Postman_Collection.json file located in the root of this repository.