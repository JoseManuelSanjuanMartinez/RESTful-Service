# RESTful Banking Service

RESTful Banking Service is a Java-based web application built with Spring Boot for managing banks, mortgage products, and associated document attachments. The platform allows authorized users to create, update, and delete financial institutions and their mortgage offerings, upload contractual PDF files, and perform mortgage simulations. It also integrates Azure AI services to extract and interpret information from uploaded mortgage documents.

The project is designed as a secure REST API with API-key authentication, resource ownership validation, and file storage for document-based mortgage records.

---

## Overview

This service is tailored to a banking and mortgage catalog use case where each bank can own multiple mortgages. The API exposes endpoints to manage banks and the mortgages associated with them, while also handling attached documentation such as PDF contracts, legal notices, risk reports, and disclaimers.

The system supports:

- Bank catalog management
- Mortgage portfolio management per bank
- Ownership enforcement for bank and mortgage resources
- PDF upload and storage for attached mortgage documents
- Download and deletion of stored attachment files
- Mortgage payment simulation based on principal, term, and APR
- AI-assisted document analysis for extracting text and enriching mortgage descriptions

---

## Technology Stack

- Java 17
- Spring Boot 3 / Spring Web MVC
- Spring Data JDBC
- Spring Security
- H2 Database (embedded / in-memory)
- Maven (wrapper included)
- Azure AI Document Intelligence
- Azure OpenAI
- Multipart file handling for PDF uploads

---

## Main Functionalities

### 1. Bank Management

The application exposes a RESTful resource for banks under the `/banks` endpoint.

Supported operations:

- `GET /banks` — returns all banks
- `GET /banks/{bankId}` — returns a bank by identifier
- `POST /banks` — creates a new bank
- `PUT /banks/{bankId}` — updates an existing bank
- `DELETE /banks/{bankId}` — deletes a bank if it is owned by the authenticated user

Each bank stores:

- `name`
- `swiftBicCode`
- `web`
- `ownerId`

Ownership is enforced by comparing the authenticated API user with the bank owner.

### 2. Mortgage Management

Mortgages are managed under the bank-scoped route `/banks/{bankId}/mortgages`.

Supported operations:

- `GET /banks/{bankId}/mortgages` — returns all mortgages for a bank
- `GET /banks/{bankId}/mortgages/{mortgageId}` — returns a specific mortgage
- `POST /banks/{bankId}/mortgages` — creates a mortgage for a bank
- `PUT /banks/{bankId}/mortgages/{mortgageId}` — updates a mortgage
- `DELETE /banks/{bankId}/mortgages/{mortgageId}` — deletes a mortgage

Mortgage fields include:

- `name`
- `interestRateType`
- `description`
- `apr` (Annual Percentage Rate)

Supported interest-rate types are:

- `FIJO`
- `MIXTO`
- `VARIABLE`

### 3. Mortgage Simulation

The API includes a mortgage simulation endpoint that calculates the monthly payment from a principal amount and loan term.

- `GET /banks/{bankId}/mortgages/{mortgageId}/simulate?principal={value}&years={years}`

The calculation uses the mortgage APR and the requested number of years to estimate the monthly installment.

### 4. Mortgage Attachment Management

The service allows financial documentation to be associated with a mortgage through file uploads.

Supported operations:

- `POST /banks/{bankId}/mortgages/{mortgageId}/attachments` — uploads a PDF attachment
- `GET /banks/{bankId}/mortgages/{mortgageId}/attachments` — lists attachments for a mortgage
- `GET /banks/{bankId}/mortgages/{mortgageId}/attachments/{attachmentId}` — downloads a stored attachment
- `DELETE /banks/{bankId}/mortgages/{mortgageId}/attachments/{attachmentId}` — removes an attachment and deletes the corresponding file from disk

Attachment types supported by the model:

- `CONDITIONS`
- `LEGAL_NOTICE`
- `DISCLAIMER`
- `RISK_REPORT`
- `OTHER`

Files are validated before persistence:

- only PDF files are accepted
- maximum upload size is 10 MB
- a PDF header check verifies the file signature starts with `%PDF`

Stored files are placed under a filesystem path such as:

```text
./data/attachments/{bankId}/{mortgageId}/
```

### 5. AI-Powered Document Analysis

After a mortgage attachment is uploaded, the application can trigger an asynchronous analysis process.

The service:

- reads the mortgage attachment binary content
- extracts text using Azure AI Document Intelligence
- sends the extracted content to Azure OpenAI for a summary or structured interpretation
- updates the mortgage description with the AI-generated result

This functionality is intended to reduce manual review effort and improve document understanding for mortgage conditions and legal terms.

---

## Security Model

The API uses an API-key authentication mechanism. Protected mutation endpoints require the `ApiKey` header to be present and valid.

Example:

```http
ApiKey: your-api-key
```

Security configuration currently allows:

- Public access to read-only endpoints for banks and mortgages
- Authentication required for create, update, and delete operations
- Ownership validation before modifying or deleting bank and mortgage resources

The project is built around a stateless security design using Spring Security and does not rely on server-side sessions.

---

## Project Structure

```text
src/
├── main/
│   ├── java/org/example/banksproject/
│   │   ├── controller/          # REST controllers for banks, mortgages, and attachments
│   │   ├── models/              # Domain entities such as Bank, Mortgage, ApiUser, and attachments
│   │   ├── repositories/        # Spring Data JDBC repositories
│   │   ├── security/            # API key authentication and security configuration
│   │   ├── service/             # Business services, including file storage
│   │   └── service/ai/          # AI document analysis integration for Azure services
│   └── resources/
│       ├── application.properties # Application configuration and storage settings
│       ├── OpenAPIDocs.yml        # OpenAPI specification
│       └── schema.sql             # Initial database schema and sample data
└── test/
    └── java/org/example/banksproject/ # Unit and integration tests
```

---

## Database and Data Model

The application uses the H2 embedded database configured through Spring Boot. Initial schema creation and sample records are defined in `src/main/resources/schema.sql`.

Core tables include:

- `API_USER`
- `BANK`
- `MORTGAGE`
- `MORTGAGE_ATTACHMENT`

Sample users are seeded in the schema for development and testing, including:

- `admin` / `admin-key`
- `test` / `test-key`

The domain model uses bank ownership and mortgage-to-bank relationships to ensure logical data isolation.

---

## Configuration

The main application configuration is stored in `src/main/resources/application.properties`.

Key settings include:

- `attachments.storage.path=./data/attachments`
- multipart upload limits:
  - `spring.servlet.multipart.max-file-size=10MB`
  - `spring.servlet.multipart.max-request-size=10MB`
- Azure Document Intelligence endpoint
- Azure OpenAI endpoint and deployment name

---

## Running the Application

### Prerequisites

- Java JDK 17 or newer
- Maven (or use the provided Maven wrapper)
- Access to Azure AI services if document analysis is enabled

### Start the application

On Linux/macOS:

```bash
./mvnw spring-boot:run
```

On Windows (PowerShell / CMD):

```powershell
.\mvnw.cmd spring-boot:run
```

The application starts by default on port `8080`.

---

## API Documentation

The application includes a full OpenAPI document in:

```text
src/main/resources/OpenAPIDocs.yml
```

This file describes the available REST endpoints, request bodies, and resource contracts for banks, mortgages, and attachments.

---

## Testing

Run the automated test suite with:

On Linux/macOS:

```bash
./mvnw test
```

On Windows:

```powershell
.\mvnw.cmd test
```

---

## Example Usage

Create a bank:

```bash
curl -X POST http://localhost:8080/banks \
  -H "Content-Type: application/json" \
  -H "ApiKey: admin-key" \
  -d '{
    "name": "Example Bank",
    "swiftBicCode": "EXAMPLEB1",
    "web": "https://examplebank.com"
  }'
```

Create a mortgage for that bank:

```bash
curl -X POST http://localhost:8080/banks/1/mortgages \
  -H "Content-Type: application/json" \
  -H "ApiKey: admin-key" \
  -d '{
    "name": "Fixed Rate Home Mortgage",
    "interestRateType": "FIJO",
    "description": "30-year fixed-rate mortgage for residential properties.",
    "apr": 3.25
  }'
```

Upload a PDF attachment associated with that mortgage:

```bash
curl -X POST "http://localhost:8080/banks/1/mortgages/1/attachments" \
  -H "ApiKey: admin-key" \
  -F "attachmentType=CONDITIONS" \
  -F "file=@conditions.pdf"
```

---

## Summary

RESTful Banking Service provides a robust foundation for managing mortgage-related financial data in a secure and extensible manner. It combines REST API design, document storage, data ownership controls, and AI-based document parsing into a single platform suitable for digital mortgage operations and bank catalog administration.
