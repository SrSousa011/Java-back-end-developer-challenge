# Java-back-end-developer-challenge




# SecureDoc

SecureDoc is a Java back-end project for cryptographic file processing, including hash generation, digital signatures (CMS), signature verification, and a REST API to interact with these functionalities. This project is part of a Java Back-end Developer challenge.

---

## Table of Contents

- [Overview](#overview)
- [Project Structure](#project-structure)
- [Requirements](#requirements)
- [Setup](#setup)
- [Usage](#usage)
- [Endpoints](#endpoints)
- [Testing](#testing)
- [File Locations](#file-locations)
- [Implementation Notes](#implementation-notes)

---

## Overview

The project implements the following functionalities:

1. **Etapa 1 – Hash Calculation:**  
   Calculate SHA-512 hash of a file (`doc.txt`) and store it in hexadecimal format.

2. **Etapa 2 – Digital Signature (CMS):**  
   Sign a document using a PKCS12 certificate, SHA-512 hash, and RSA asymmetric encryption. Generates an attached CMS signature (`.p7s`).

3. **Etapa 3 – Signature Verification:**  
   Verify CMS signatures, ensuring document integrity and certificate trust, returning status and signer info.

4. **Etapa 4 – REST API:**  
   Expose endpoints to sign documents and verify signatures using Spring Boot 3.x.

---

## Project Structure

securedoc/
│
├─ src/main/java/com/yourname/securedoc/
│ ├─ SecureDocApplication.java # Spring Boot entry point
│ ├─ controller/
│ │ └─ SignatureController.java # REST API endpoints
│ ├─ service/
│ │ └─ SignatureService.java # Core logic for hashing/signing/verifying
│ ├─ util/ # Utility classes (file handling, crypto helpers)
│ └─ model/ # Request/response DTOs
│
├─ src/main/resources/
│ ├─ arquivos/ # Input files (e.g., doc.txt)
│ ├─ pkcs12/ # PKCS12 certificates
│ └─ cadeia/ # Certificate chain for trust verification
│
├─ src/test/java/com/yourname/securedoc/
│ └─ ... # JUnit test cases
│
├─ pom.xml # Maven project configuration
└─ README.md

---

## Requirements

- Java 17 (or 11)
- Maven 3.x
- Spring Boot 3.x
- BouncyCastle (bcprov, bcpkix)
- Commons IO
- JUnit 5

---

## Setup

1. Clone the repository:
```bash
git clone <your-repo-url>
cd securedoc


2. Build the project:

mvn clean install


Run the application:

3. mvn spring-boot:run

The server will start on http://localhost:8080.


Usage
Hashing
•	Calculate SHA-512 hash for doc.txt.
•	The output is stored in hexadecimal.
Signing
•	Input: file to sign, PKCS12 certificate, password.
•	Output: .p7s CMS attached signature.
Verification
•	Input: signed CMS file.
•	Output: JSON with:
o	status: VALIDO / INVALIDO
o	infos: signer CN, signingTime, hash (hex), digest algorithm


Endpoints
POST /signature
•	Content-Type: multipart/form-data
•	Parameters:
o	file: file to sign
o	pkcs12: PKCS12 certificate file
o	password: certificate password
•	Response: Base64 encoded CMS signature
POST /verify
•	Content-Type: multipart/form-data
•	Parameters:
o	file: signed CMS file


•	Response:

JSON

{
  "status": "VALIDO",
  "infos": {
    "signer": "John Doe",
    "signingTime": "2025-08-29T15:00:00Z",
    "hash": "ABC123...",
    "digestAlgorithm": "SHA-512"
  }
}


Testing

Unit tests are located in src/test/java/com/yourname/securedoc/.
Run tests using Maven:

mvn test





File Locations
Purpose	Path
Input document	    src/main/resources/arquivos/doc.txt
PKCS12 certificates	src/main/resources/pkcs12/
Certificate chain	src/main/resources/cadeia/
Generated CMS signature	.p7s file in output directory (configurable)
Hash output file	hash.txt (hexadecimal)





Implementation Notes
•	SHA-512 is used as the cryptographic hash algorithm.
•	CMS signatures are generated using BouncyCastle (CMSSignedDataGenerator).
•	Signature verification validates both the integrity of the file and the trust chain of the certificate.
•	Spring Boot REST endpoints are built for multipart file handling.
•	Proper error handling and logging are implemented for robustness.
•	Unit tests cover all core functionalities.