# Java Back-end Developer Challenge

## SecureDoc

SecureDoc is a Java back-end project for cryptographic file processing. It includes:

- SHA-512 hash generation of files
- Digital signature generation (CMS attached)
- Signature verification
- REST API to interact with these functionalities

This project is part of a Java Back-end Developer challenge and demonstrates the use of Spring Boot, BouncyCastle, and best practices for back-end development.

---

## Table of Contents

- [Overview](#overview)
- [Project Structure](#project-structure)
- [Requirements](#requirements)
- [Setup](#setup)
- [Usage](#usage)
- [Endpoints](#endpoints)
- [Swagger UI](#swagger-ui)
- [Testing](#testing)
- [File Locations](#file-locations)
- [Implementation Notes](#implementation-notes)

---

## Overview

The project implements the following:

1. **Etapa 1 – Hash Calculation:**  
   Compute the SHA-512 hash of `doc.txt` and save it in hexadecimal format.

2. **Etapa 2 – Digital Signature (CMS):**  
   Sign a document using a PKCS12 certificate, SHA-512 hash, and RSA. Produces an attached CMS signature (`.p7s`).

3. **Etapa 3 – Signature Verification:**  
   Verify CMS signatures, checking both integrity and certificate trust. Returns status and signer info.

4. **Etapa 4 – REST API:**  
   Expose endpoints to sign documents and verify signatures with Spring Boot 3.x.

---

## Project Structure

securedoc/
│
├─ src/main/java/org/lucassousa/securedoc/
│  ├─ SecureDocApplication.java           # Spring Boot entry point
│  ├─ controller/
│  │  └─ SignatureController.java        # REST API endpoints
│  ├─ service/
│  │  └─ SignatureService.java           # Core logic: hash/sign/verify
│  ├─ util/                              # Utilities (file handling, crypto helpers)
│  └─ config/
│     └─ OpenApiConfig.java              # Swagger/OpenAPI configuration
│
├─ src/main/resources/
│  ├─ arquivos/      # Input files (e.g., doc.txt)
│  ├─ pkcs12/        # PKCS12 certificates
│  └─ cadeia/        # Certificate chain for trust verification
│
├─ src/test/java/org/lucassousa/securedoc/
│  └─ ...           # JUnit test cases
│
├─ pom.xml           # Maven configuration
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

## Usage

The server will start at http://localhost:8080

### Hashing
- Calculate SHA-512 hash for doc.txt.
- Output is stored in hexadecimal format.

### Signing
- Input: file to sign, PKCS12 certificate, password.
- Output: `.p7s` CMS attached signature.

### Verification
- Input: signed CMS file.
- Output: JSON with:

