🛒 Marketplace System

A backend system for an e-commerce Marketplace, built with Java Spring Boot.
This project provides APIs for user authentication, product catalog, order management, shopping cart, and payments. It supports JWT authentication, MinIO for file storage, and Redis caching.

👥 User Roles and Features
Customer Users

Browse Marketplace: View homepage and product catalog.

Product Management: Search and filter products by category.

Shopping Cart: Add products to cart and checkout.

Orders: View purchase history and invoices.

Profile: Edit personal information and manage avatar.

Feedback: Rate and review purchased products.

Sellers

Same features as Customer Users, plus:

Product Management: Add, edit, or remove owned products.

Order Management: Handle customer orders and shipping.

Sales Analytics: Track revenue and statistics.

Administrators

User Management: Manage all users.

Category Management: Add/edit product categories.

Discount Management: Create and manage discount campaigns.

Global Product Management: Moderate all products and sellers.

🛠 Additional Features

Authentication: JWT access/refresh token handling with Redis for token revocation.

Payments: Integrated with VNPay and MoMo for secure transactions.

Social Login: Register/login with Google or Facebook.

File Storage: Product images stored in MinIO with presigned URLs.

Rate Limiting: Custom annotation-based rate limiting via Spring AOP.

Logging: Centralized logging with ELK stack (Elasticsearch, Logstash, Kibana).

Async Tasks: Kafka integration for email notifications and metadata handling.

Monitoring: Prometheus, Grafana, and Tempo for observability.

🏗 Core Technologies

Spring Boot (Security, JPA, Web, Validation)

MySQL (database)

Redis (cache, JWT revoked tokens)

MinIO (S3-compatible object storage)

Kafka (asynchronous events)

Swagger / OpenAPI (API docs)

Docker & Docker Compose (deployment)

🐳 Setup with Docker

Install Docker Desktop.

Go to the project root, open terminal:

docker-compose -f docker-compose.yml up -d


(Wait for containers to be pulled and started for the first time.)

Open Docker Desktop → Containers → marketplace-system to see running containers.

Access phpMyAdmin: http://localhost:8000

Database name: marketplace_db

📂 Project Structure

entities → JPA entities (User, Product, Order, Category, etc.)

repositories → JPA Repositories for database access

services → Business logic layer

impls → Implementations of service interfaces

controllers → REST API endpoints

configs → Application configurations

security → Security & JWT configs

OpenAPIConfig.java → Swagger / OpenAPI setup

LocaleConfig.java → Multi-language support

components → Helper components (JWT decoder, Translator, etc.)

exceptions → Custom exception handling (with GlobalExceptionHandler)

mappers → MapStruct mappers (Entity ↔ DTO)

dtos → Data Transfer Objects (request & response)

resources

i18n/messages.properties (English)

i18n/messages_vi.properties (Vietnamese)

application.yml (app config)

📖 API Documentation

After running the application, open Swagger UI:
👉 http://localhost:8085/marketplace/swagger-ui/index.html

Get JSON file from Swagger

Open http://localhost:8085/marketplace/swagger-ui/index.html
.

In the top-left corner, below the title, find the URL /marketplace/api-docs/rest-api-service-dev.

Open it directly: http://localhost:8085/marketplace/api-docs/rest-api-service-dev
.

Copy all JSON → save as .json file.

Import into Postman via "Import → File".
