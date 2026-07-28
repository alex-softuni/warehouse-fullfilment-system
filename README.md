# Warehouse Fulfilment System

A RESTful backend application for managing products, warehouse inventory, stock movements, and multi-item customer orders.

The project demonstrates transactional business logic with Spring Boot and PostgreSQL, including stock reservation, order cancellation and shipping, database migrations, containerized deployment, and automated testing.

## Highlights

- Product creation, retrieval, updates, archiving, and restoration
- Automatic inventory creation for every new product
- Receiving, reserving, releasing, and shipping stock
- Inventory transaction history
- Transactional multi-item order creation
- SKU, product-name, and unit-price snapshots in order items
- Order retrieval, pagination, cancellation, and shipping
- Validation of order status transitions
- Transactional rollback when an operation fails
- Flyway-managed PostgreSQL schema
- Dockerized application and database
- PostgreSQL integration testing with Testcontainers
- Automated Maven tests with GitHub Actions

## Technology Stack

- Java 26
- Spring Boot 4.1
- Spring Web
- Spring Data JPA and Hibernate
- PostgreSQL
- Flyway
- Maven Wrapper
- Docker and Docker Compose
- JUnit 5 and Mockito
- Testcontainers
- GitHub Actions

## Architecture

The application follows a layered architecture and is organized by business area.

```text
HTTP Request
    ↓
Controller
    ↓
Service
    ↓
Repository
    ↓
PostgreSQL
```

- **Controller layer** — exposes REST endpoints, validates request DTOs, and returns response DTOs.
- **Service layer** — contains business rules and coordinates product, inventory, order, and transaction operations.
- **Repository layer** — uses Spring Data JPA to persist and retrieve entities.
- **Domain layer** — contains the main entities and their relationships: `Product`, `Inventory`, `InventoryTransaction`, `Order`, and `OrderItem`.

Transactional service methods ensure that multi-step operations either complete fully or roll back completely.

## Business Workflows

### Product and Inventory

Creating a product automatically creates one corresponding inventory record.

Each inventory tracks:

- Available quantity
- Reserved quantity

```text
physical quantity = available quantity + reserved quantity
```

Supported inventory operations are:

- Receive stock
- Reserve available stock
- Release reserved stock
- Ship reserved stock

Every stock movement creates an `InventoryTransaction` record for audit history.

### Order Creation

When a multi-item order is created, the application:

1. Validates the customer, delivery address, and requested items.
2. Loads and validates every product.
3. Confirms that sufficient stock is available for every item.
4. Reserves the required quantities.
5. Creates an order with status `CONFIRMED`.
6. Stores product SKU, name, and unit-price snapshots in each `OrderItem`.

Each order item stores:

- A reference to the product
- The ordered quantity
- A snapshot of the product SKU
- A snapshot of the product name
- A snapshot of the unit price

The snapshots preserve the original commercial details even if a product is renamed or its price changes later.

### Transactional Rollback

Order creation is transactional. If any requested product is missing or has insufficient stock, the entire operation is rolled back.

```text
Product 1 reservation → rolled back
Product 2 reservation → rolled back
Order creation        → rolled back
```

This prevents partially created orders and partially reserved inventory.

### Order Lifecycle

```text
CONFIRMED → CANCELLED
CONFIRMED → SHIPPED
```

Cancelling a confirmed order releases all reserved quantities back to available stock.

Shipping a confirmed order removes the reserved quantities from physical stock and records the stock movements.

Orders that are already `CANCELLED` or `SHIPPED` cannot be cancelled or shipped again.

## API Reference

Base URL:

```text
http://localhost:8080
```

### Products

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/products` | Create a product and its inventory |
| `GET` | `/api/products` | Retrieve all active products |
| `GET` | `/api/products/{id}` | Retrieve a product by ID |
| `PUT` | `/api/products/{id}` | Update a product |
| `PATCH` | `/api/products/{id}/archive` | Archive a product |
| `GET` | `/api/products/archived` | Retrieve archived products |
| `PATCH` | `/api/products/{id}/restore` | Restore an archived product |

### Inventory

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/inventory/product/{productId}` | Retrieve inventory for a product |
| `POST` | `/api/inventory/receive` | Receive stock |
| `POST` | `/api/inventory/reserve` | Reserve available stock |
| `POST` | `/api/inventory/release` | Release reserved stock |
| `POST` | `/api/inventory/ship` | Ship reserved stock |
| `GET` | `/api/inventory/product/{productId}/transactions` | Retrieve transaction history |

### Orders

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/orders` | Create a multi-item order |
| `GET` | `/api/orders` | Retrieve paginated orders |
| `GET` | `/api/orders/{orderId}` | Retrieve an order by ID |
| `PATCH` | `/api/orders/{orderId}/cancel` | Cancel a confirmed order |
| `PATCH` | `/api/orders/{orderId}/ship` | Ship a confirmed order |

### Order Pagination

```http
GET http://localhost:8080/api/orders?page=0&size=5&sort=createdAt,desc
```

Default pagination:

```text
Page:      0
Page size: 20
Sort:      createdAt descending
```

## Request Examples

The examples below form a typical runnable flow:

```text
Create product → receive stock → create order → cancel or ship order
```

### 1. Create a Product

```http
POST http://localhost:8080/api/products
Content-Type: application/json

{
  "sku": "LAPTOP-001",
  "name": "Business Laptop",
  "price": 1299.99
}
```

Example response:

```json
{
  "id": "7b546fb6-1fa8-4c16-b558-e4fa159d8ac8",
  "sku": "LAPTOP-001",
  "name": "Business Laptop",
  "price": 1299.99,
  "active": true
}
```

Creation returns `201 Created` and automatically creates inventory with zero available and reserved quantities.

Validation rules:

```text
sku    required, maximum 50 characters, unique
name   required, maximum 150 characters
price  required, zero or greater
```

<details>
<summary>Additional product requests</summary>

Retrieve all active products:

```http
GET http://localhost:8080/api/products
```

Retrieve a product by ID:

```http
GET http://localhost:8080/api/products/7b546fb6-1fa8-4c16-b558-e4fa159d8ac8
```

Update a product:

```http
PUT http://localhost:8080/api/products/7b546fb6-1fa8-4c16-b558-e4fa159d8ac8
Content-Type: application/json

{
  "name": "Business Laptop Pro",
  "price": 1399.99
}
```

The SKU is not part of the update request and remains unchanged.

Archive a product:

```http
PATCH http://localhost:8080/api/products/7b546fb6-1fa8-4c16-b558-e4fa159d8ac8/archive
```

Retrieve archived products:

```http
GET http://localhost:8080/api/products/archived
```

Restore a product:

```http
PATCH http://localhost:8080/api/products/7b546fb6-1fa8-4c16-b558-e4fa159d8ac8/restore
```

</details>

### 2. Receive and Manage Stock

Replace the example `productId` with the ID returned when creating a product.

Receive stock:

```http
POST http://localhost:8080/api/inventory/receive
Content-Type: application/json

{
  "productId": "7b546fb6-1fa8-4c16-b558-e4fa159d8ac8",
  "quantity": 25
}
```

Example inventory response:

```json
{
  "id": "fceaa24b-ec53-43d3-b73b-ea931741437f",
  "productId": "7b546fb6-1fa8-4c16-b558-e4fa159d8ac8",
  "productSku": "LAPTOP-001",
  "availableQuantity": 25,
  "reservedQuantity": 0,
  "physicalQuantity": 25
}
```

All inventory modification requests require a `productId` and a quantity greater than zero.

<details>
<summary>Additional inventory requests</summary>

Reserve stock:

```http
POST http://localhost:8080/api/inventory/reserve
Content-Type: application/json

{
  "productId": "7b546fb6-1fa8-4c16-b558-e4fa159d8ac8",
  "quantity": 5
}
```

Release reserved stock:

```http
POST http://localhost:8080/api/inventory/release
Content-Type: application/json

{
  "productId": "7b546fb6-1fa8-4c16-b558-e4fa159d8ac8",
  "quantity": 2
}
```

Ship reserved stock:

```http
POST http://localhost:8080/api/inventory/ship
Content-Type: application/json

{
  "productId": "7b546fb6-1fa8-4c16-b558-e4fa159d8ac8",
  "quantity": 3
}
```

Retrieve inventory:

```http
GET http://localhost:8080/api/inventory/product/7b546fb6-1fa8-4c16-b558-e4fa159d8ac8
```

Retrieve transaction history:

```http
GET http://localhost:8080/api/inventory/product/7b546fb6-1fa8-4c16-b558-e4fa159d8ac8/transactions
```

Example transaction response:

```json
[
  {
    "id": "70e2ca4d-c4dc-483c-b365-c051683ba134",
    "productId": "7b546fb6-1fa8-4c16-b558-e4fa159d8ac8",
    "productSku": "LAPTOP-001",
    "type": "STOCK_RECEIVED",
    "quantity": 25,
    "createdAt": "2026-07-28T14:30:00"
  }
]
```

</details>

### 3. Create a Multi-Item Order

```http
POST http://localhost:8080/api/orders
Content-Type: application/json

{
  "customerName": "Alexander Ivanov",
  "customerEmail": "alexander@example.com",
  "address": {
    "country": "Bulgaria",
    "city": "Sofia",
    "postalCode": "1000",
    "street": "Vitosha Boulevard",
    "addressLine": "Apartment 12"
  },
  "items": [
    {
      "productId": "7b546fb6-1fa8-4c16-b558-e4fa159d8ac8",
      "quantity": 2
    },
    {
      "productId": "aacb385f-3643-480e-9618-474207dd52f5",
      "quantity": 1
    }
  ]
}
```

Creation returns `201 Created`.

Order validation rules:

```text
customerName   required, between 2 and 40 characters
customerEmail  required, valid email address
address        required
country        required
city           required
postalCode     required
street         required
addressLine    optional
items          between 1 and 100 items
productId      required for each item
quantity       greater than zero
```

Example response:

```json
{
  "orderId": "85091a28-e9dd-4809-8639-1575542bfa76",
  "status": "CONFIRMED",
  "customerName": "Alexander Ivanov",
  "customerEmail": "alexander@example.com",
  "address": {
    "country": "Bulgaria",
    "city": "Sofia",
    "postalCode": "1000",
    "street": "Vitosha Boulevard",
    "addressLine": "Apartment 12"
  },
  "items": [
    {
      "productId": "7b546fb6-1fa8-4c16-b558-e4fa159d8ac8",
      "productSku": "LAPTOP-001",
      "productName": "Business Laptop",
      "quantity": 2,
      "unitPrice": 1299.99,
      "lineTotal": 2599.98
    },
    {
      "productId": "aacb385f-3643-480e-9618-474207dd52f5",
      "productSku": "MOUSE-001",
      "productName": "Wireless Mouse",
      "quantity": 1,
      "unitPrice": 49.99,
      "lineTotal": 49.99
    }
  ],
  "totalPrice": 2649.97,
  "createdAt": "2026-07-28T15:00:00",
  "updatedAt": "2026-07-28T15:00:00"
}
```

<details>
<summary>Additional order requests</summary>

Retrieve an order:

```http
GET http://localhost:8080/api/orders/85091a28-e9dd-4809-8639-1575542bfa76
```

Retrieve paginated orders:

```http
GET http://localhost:8080/api/orders?page=0&size=5&sort=createdAt,desc
```

Cancel a confirmed order:

```http
PATCH http://localhost:8080/api/orders/85091a28-e9dd-4809-8639-1575542bfa76/cancel
```

Ship a confirmed order:

```http
PATCH http://localhost:8080/api/orders/85091a28-e9dd-4809-8639-1575542bfa76/ship
```

Only confirmed orders can be cancelled or shipped.

</details>

## Running with Docker

### Prerequisites

- Docker with Docker Compose
- Git

A local PostgreSQL installation is not required.

### 1. Clone the repository

```bash
git clone <repository-url>
cd warehouse-fullfilment-system
```

Replace `<repository-url>` with the repository's HTTPS URL.

### 2. Create the environment file

Linux or macOS:

```bash
cp .env.example .env
```

Windows PowerShell:

```powershell
Copy-Item .env.example .env
```

Configure `.env`:

```env
POSTGRES_DB=warehouse_flyway_db
POSTGRES_USER=postgres
POSTGRES_PASSWORD=change_me
```

The `.env` file is ignored by Git and must not be committed.

### 3. Build and start the system

```bash
docker compose up -d --build
```

Docker Compose will:

1. Build the Spring Boot application image.
2. Start PostgreSQL.
3. Wait until PostgreSQL reports a healthy status.
4. Start the Spring Boot application.
5. Apply pending Flyway migrations.
6. Validate the schema with Hibernate.

### 4. Verify the containers

```bash
docker compose ps
```

Expected containers:

```text
warehouse-postgres
warehouse-app
```

### 5. View application logs

```bash
docker compose logs -f app
```

### 6. Stop the system

Preserve the PostgreSQL volume:

```bash
docker compose down
```

Delete the containers and database volume:

```bash
docker compose down -v
```

Use `-v` only when a completely fresh database is required.

## Running the Tests

Docker must be running because the integration test starts a temporary PostgreSQL container with Testcontainers.

Windows PowerShell:

```powershell
.\mvnw.cmd clean test
```

Linux or macOS:

```bash
./mvnw clean test
```

The test process:

1. Starts a temporary PostgreSQL container.
2. Applies the Flyway migrations.
3. Validates the schema with Hibernate.
4. Runs the unit and integration tests.
5. Removes the temporary container.

No manually configured test database or local PostgreSQL credentials are required.

The test suite covers successful and failing inventory and order scenarios, including transactional rollback and invalid status transitions.

## Database Management

The application uses Flyway for version-controlled PostgreSQL schema migrations.

The initial schema is defined in:

```text
src/main/resources/db/migration/V1__initial_schema.sql
```

Application tables:

```text
products
inventories
inventory_transactions
orders
order_items
```

Flyway also creates and maintains its own `flyway_schema_history` table.

### Main Relationships

```text
Product 1 ─── 1 Inventory
Product 1 ─── * InventoryTransaction
Order   1 ─── * OrderItem
Product 1 ─── * OrderItem
```

Important constraints include:

- Unique product SKU
- One inventory record per product
- Required foreign-key relationships
- Required order snapshot fields
- Non-null inventory quantities
- Numeric precision for product and order prices

Hibernate is configured with:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

Startup order:

```text
PostgreSQL starts
    ↓
Flyway applies pending migrations
    ↓
Hibernate validates the schema
    ↓
Spring Boot starts
```

Applied migration files must not be edited. Future schema changes should use new versions, for example:

```text
V1__initial_schema.sql
V2__add_order_reference.sql
V3__add_inventory_indexes.sql
```

## Continuous Integration

GitHub Actions runs the full Maven test suite when:

- Code is pushed to `master`
- A pull request targets `master`

The workflow uses Ubuntu, Amazon Corretto Java 26, Maven Wrapper, Docker, and PostgreSQL Testcontainers.

Workflow file:

```text
.github/workflows/ci.yml
```
