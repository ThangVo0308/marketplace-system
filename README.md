# 🛒 Marketplace System

A backend system for an e-commerce Marketplace, built with **Java Spring Boot**.  
This project provides APIs for user authentication, product catalog, order management, shopping cart, and payments.  
It supports **JWT authentication, MinIO for file storage,...**.

---

## 👥 User Roles and Features

### Customer Users  
- Browse Marketplace: View homepage and product catalog.  
- Product Management: Search and filter products by category.  
- Shopping Cart: Add products to cart and checkout.  
- Orders: View purchase history and invoices.  
- Profile: Edit personal information and manage avatar.  

### Sellers  
- All features of Customer Users, plus:  
- Product Management: Add, edit, or remove owned products.  
- Order Management: Handle customer orders and shipping.  
- Sales Analytics: Track revenue and statistics.  

### Administrators  
- User Management: Manage all users.  
- Category Management: Add/edit product categories.  
- Discount Management: Create and manage discount campaigns.  

---

## 🛠 Additional Features  
- **Authentication:** JWT access/refresh token handling with Redis for token revocation.  
- **Payments:** Integrated with VNPay(Momo in the future) for secure transactions.  
- **Social Login:** Register/login with Google or Facebook.  
- **File Storage:** Product images stored in MinIO with presigned URLs.  
- **Rate Limiting:** Custom annotation-based rate limiting via Spring AOP.  
- **Logging:** Centralized logging with ELK stack (Elasticsearch, Logstash, Kibana).  
- **Monitoring:** Prometheus, Grafana, and Tempo for observability.  

---

## 🏗 Core Technologies  
- Spring Boot (Security, JPA, Web, Validation)  
- MySQL (database)  
- Redis (cache, JWT revoked tokens)  
- MinIO (S3-compatible object storage)  
- Swagger / OpenAPI (API docs)  
- Docker & Docker Compose (deployment)  

---

## 🐳 Setup with Docker  

1. Install **Docker Desktop**.  
2. Open terminal at project root:  

   ```bash
   docker-compose -f docker-compose.yml up -d
(Wait for containers to be pulled and started for the first time.)

3. Open Docker Desktop → **Containers** → `marketplace-system` to see running containers.  

4. Access **phpMyAdmin**: [http://localhost:8000](http://localhost:8000)  
- Database name: `marketplace_db`  

---

## 📂 Project Structure

- **entities** → JPA entities (User, Product, Order, Category, etc.)  
- **repositories** → JPA Repositories for database access  
- **services** → Business logic layer  
  - `impls` → Implementations of service interfaces  
- **controllers** → REST API endpoints  
- **configs** → Application configurations  
  - `security` → Security & JWT configs  
  - `OpenAPIConfig.java` → Swagger / OpenAPI setup  
  - `LocaleConfig.java` → Multi-language support  
- **components** → Helper components (JWT decoder, Translator, etc.)  
- **exceptions** → Custom exception handling (with GlobalExceptionHandler)  
- **mappers** → MapStruct mappers (Entity ↔ DTO)  
- **dtos** → Data Transfer Objects (request & response)  
- **resources**  
  - `i18n/messages.properties` (English)  
  - `i18n/messages_vi.properties` (Vietnamese)  
  - `application.yml` (app config)  

---

## 📖 API Documentation

After running the application, open Swagger UI:  
👉 [http://localhost:8085/marketplace/swagger-ui/index.html](http://localhost:8085/marketplace/swagger-ui/index.html)  

### Get JSON file from Swagger  
1. Go to Swagger UI: [http://localhost:8085/marketplace/swagger-ui/index.html](http://localhost:8085/marketplace/swagger-ui/index.html)  
2. In the top-left corner, find the URL:  /marketplace/api-docs/rest-api-service-dev
3. Open it directly:  
[http://localhost:8085/marketplace/api-docs/rest-api-service-dev](http://localhost:8085/marketplace/api-docs/rest-api-service-dev)  
4. Copy all JSON → save as `.json` file.  
5. Import into Postman: `Import → File`.  


