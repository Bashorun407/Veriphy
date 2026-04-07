# Project Name: Veriphy
## Author: Oluwaseyi Akinbobola
## Date: 2nd April 2026

Project Description:
This project implements a document issuance service that renders official PDFs (transcript/certificate) with QR code linking to a verification endpoint, stores files in object storage (locl/minio/S3-compatible), and issues time-limited download links. 
It further includes hashings/signature metadata and an admin dashboard API to revoke documents.

# Veriphy: Secure Document Issuance & Verification Service

##  Project Overview
Veriphy is a production-grade backend service designed to solve the problem of academic and official document forgery. It provides a complete workflow for generating physical, QR-coded PDF documents (like transcripts and certificates) and prove their authenticity through an automated upload-and-verify API.

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
Create a new database in PostgreSQL and update the application.properties (or application.yaml) file and run the
application.
---

# Distinction Certificates
### 1. Introduction to Object Oriented Programming Link: https://distinction.app/public-certificate/f5be51a8-1689-4c93-870c-00349a482bdf


### 2. Mastering Productivity, Collaboration & Problem Solving for High-Performers Link: https://distinction.app/public-certificate/f4e0fda4-04b0-4de1-a606-c650920dc91e

# Link to Hosted API (Render)
### https://veriphy-43h5.onrender.com

# Path to Project Slide:
### https://github.com/Bashorun407/Veriphy/blob/58dbf82d85d9449d0b5bd24e4900577e84a7574e/docs/Veriphy%20Slides/Veriphy%20Slides.pdf

# Path to Screenshot Folder:
### https://github.com/Bashorun407/Veriphy/blob/eb3296d1e5e85e226cbf05f1d4c1669baa7e399a/src/main/resources/screenshots
