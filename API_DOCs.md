
***


# Veriphy: Technical & API Documentation

## 1. System Architecture Overview
Veriphy is a stateless Spring Boot microservice designed to handle the secure lifecycle of academic and official documents. It operates on a Zero-Trust verification model using cryptographic hashing (SHA-256).

### 1.1 Core Components
* **Authentication Layer:** Spring Security with JSON Web Tokens (JWT).
* **Business Logic:** Spring Service layer handling database transactions, PDF generation (Thymeleaf/Flying Saucer), and QR code injection (ZXing).
* **Persistence:** PostgreSQL relational database.
* **Schema Management:** Flyway migrations ensure deterministic database states across environments.

---

## 2. Data Dictionary (Schema)

### Table: `document_records`
| Column Name | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | UUID | PRIMARY KEY | Manually assigned UUID (unguesable). |
| `recipient_id` | VARCHAR | NOT NULL | The ID of the student/recipient (e.g., MAT123). |
| `document_type` | VARCHAR | NOT NULL | Enum: `CERTIFICATE`, `TRANSCRIPT`, etc. |
| `document_hash` | VARCHAR | NOT NULL, UNIQUE | SHA-256 hash of the physical PDF file. |
| `storage_path` | VARCHAR | NOT NULL | Internal server/cloud path to the file. |
| `status` | VARCHAR | NOT NULL | Enum: `VALID`, `REVOKED`. |
| `issued_at` | TIMESTAMP | NOT NULL | Auto-generated timestamp on creation. |
| `expires_at` | TIMESTAMP | NULL | Optional expiration for time-sensitive docs. |

---

## 3. API Reference

### 3.1 Authentication

#### Generate Admin Token
Generates a signed JWT with a 1-hour expiration for administrative actions.
* **URL:** `/api/v1/auth/token`
* **Method:** `GET`
* **Auth Required:** No (Public for testing purposes)
* **Response (200 OK):**
  ```text
  eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJTdXBlckFkb...
  ```

---

### 3.2 Document Management (Admin Only)
*All endpoints in this section require a valid Bearer Token in the `Authorization` header.*

#### Issue New Document
Generates a secure PDF, calculates its hash, and saves the record.
* **URL:** `/api/v1/documents/issue`
* **Method:** `GET`
* **Query Parameters:**
    * `studentId` (String, Required)
    * `type` (Enum, Required) - e.g., `CERTIFICATE`
* **Response (200 OK):**
  ```json
  {
      "id": "a3930af0-2b45-4749-a786-e1291024c797",
      "recipientId": "MAT12345",
      "documentType": "CERTIFICATE",
      "documentHash": "cf1b80998c863675fbd91a00...",
      "status": "VALID",
      "issuedAt": "2026-04-02T21:06:10.793"
  }
  ```

#### Revoke Document
Acts as a kill-switch, changing the document's status to `REVOKED`.
* **URL:** `/api/v1/documents/{id}/revoke`
* **Method:** `PATCH`
* **Path Variables:** `id` (UUID of the document)
* **Response (200 OK):**
  ```text
  SUCCESS: Document {id} has been successfully REVOKED.
  ```

#### Generate Secure Download Link
Creates a time-limited (24-hour) JWT download URL for the recipient.
* **URL:** `/api/v1/documents/{id}/download-link`
* **Method:** `GET`
* **Path Variables:** `id` (UUID of the document)
* **Response (200 OK):**
  ```text
 Secure Download Link (Valid for 24h): http://.../api/v1/documents/download?token=eyJhbG...
  ```

---

### 3.3 Public Services
*These endpoints do not require an Admin Bearer Token.*

#### Verify Physical Document
Recalculates the SHA-256 hash of an uploaded PDF and checks it against the database ledger.
* **URL:** `/api/v1/documents/verify`
* **Method:** `POST`
* **Content-Type:** `multipart/form-data`
* **Body:**
    * `file`: (Type: File) The physical PDF document.
* **Responses:**
    * **200 OK (Authentic):**
      `VERIFICATION SUCCESS: This is an authentic CERTIFICATE issued to MAT12345`
    * **403 Forbidden (Revoked):**
      `VERIFICATION FAILED: This document exists but its status is: REVOKED`
    * **404 Not Found (Forged/Altered):**
      `Verification Failed: Document is invalid, altered, or does not exist.`

#### Download Document
Consumes a time-limited token to download a file.
* **URL:** `/api/v1/documents/download`
* **Method:** `GET`
* **Query Parameters:**
    * `token` (String, Required) - The 24-hour JWT token.
* **Response (200 OK):**
    * Downloads the physical `application/pdf` file to the client.
* **Response (403 Forbidden):**
    * `Download Failed: Link is invalid or has expired.`

---

## 4. Security & Cryptography Notes
* **File Mutability:** PDFs are strictly immutable once generated. Any attempt to modify text, metadata, or images will fundamentally alter the byte array, resulting in a mismatched SHA-256 hash during the `/verify` cycle.
* **Stateless Auth:** JWTs are verified strictly via the `HMAC256` secret key stored in the environment variables, preventing the need for active session management in the database.
```