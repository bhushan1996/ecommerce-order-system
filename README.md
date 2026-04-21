# E-commerce Order Processing System

A production-grade backend system for order processing built with Spring Boot 3, implementing microservices best practices, design patterns, and resilience patterns.

## 📋 Table of Contents

- [Tech Stack](#tech-stack)
- [Features](#features)
- [Architecture & Design Patterns](#architecture--design-patterns)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Resilience Patterns](#resilience-patterns)
- [Testing](#testing)
- [AI Usage & Development Notes](#ai-usage--development-notes)

## 🛠 Tech Stack

- **Java 17+**
- **Spring Boot 3.5.10**
- **Spring Data JPA** (Hibernate)
- **H2 Database** (in-memory for development)
- **PostgreSQL** (optional for production)
- **Swagger/OpenAPI 3.0** (API documentation)
- **Resilience4j** (Circuit Breaker, Rate Limiter, Bulkhead, Retry)
- **Lombok** (reduce boilerplate)
- **MapStruct** (object mapping)
- **Gradle** (build tool)
- **Docker** (containerization)
- **JUnit 5 & Mockito** (testing)

## ✨ Features

### Functional Requirements

1. **Create Order** - Accept orders with multiple items, generate unique order ID
2. **Get Order by ID** - Retrieve order details
3. **List Orders** - Get all orders with optional status filtering
4. **Update Order Status** - Transition orders through lifecycle states
5. **Cancel Order** - Cancel orders (only in PENDING state)
6. **Background Job** - Automatically process PENDING orders every 5 minutes

### Order Status Lifecycle

```
PENDING → PROCESSING → SHIPPED → DELIVERED
   ↓
CANCELLED (only from PENDING)
```

## 🏗 Architecture & Design Patterns

### 1. **Clean Architecture**

```
Controller → Service → Repository
     ↓          ↓
    DTO      Entity
     ↓
  Mapper
```

### 2. **Design Patterns Implemented**

#### **Strategy Pattern** - Order Status Transitions
- Interface: `OrderStatusTransitionStrategy`
- Implementations:
  - `ProcessingStatusStrategy` (PENDING → PROCESSING)
  - `ShippedStatusStrategy` (PROCESSING → SHIPPED)
  - `DeliveredStatusStrategy` (SHIPPED → DELIVERED)
  - `CancelledStatusStrategy` (PENDING → CANCELLED)

**Why Strategy Pattern?**
- Encapsulates status transition logic
- Easy to add new status transitions
- Follows Open/Closed Principle

#### **Factory Pattern** - Order Creation
- `OrderFactory` - Encapsulates complex order creation logic
- Uses Builder pattern internally for entity construction

**Why Factory Pattern?**
- Centralizes order creation logic
- Handles complex object initialization
- Easy to modify creation logic without affecting clients

#### **Builder Pattern** - Entity Construction
- Used in `Order` and `OrderItem` entities via Lombok `@Builder`
- Provides fluent API for object creation

#### **Exception Handling Pattern**
- `GlobalExceptionHandler` with `@ControllerAdvice`
- Custom exceptions: `OrderNotFoundException`, `InvalidOrderStateException`
- Consistent error responses across all endpoints

### 3. **Resilience Patterns (Resilience4j)**

#### **Circuit Breaker**
- Applied to `PaymentService` calls
- Configuration:
  - Sliding window: 10 calls
  - Failure threshold: 50%
  - Wait duration in open state: 10s
- Fallback method returns user-friendly error

#### **Rate Limiter**
- Applied to all API endpoints
- Limit: 10 requests per second
- Prevents API abuse and ensures fair usage

#### **Bulkhead**
- Applied to order processing operations
- Max concurrent calls: 5
- Prevents resource exhaustion

#### **Retry**
- Applied to order service operations
- Max attempts: 3
- Exponential backoff with multiplier: 2

## 📁 Project Structure

```
ecommerce-order-system/
├── src/
│   ├── main/
│   │   ├── java/com/ecommerce/orderservice/
||  │   │   ├── config/
│   │   │   │   ├── SwaggerCOnfig.java
│   │   │   ├── controller/
│   │   │   │   └── OrderController.java
│   │   │   ├── dto/
│   │   │   │   ├── ApiResponse.java
│   │   │   │   ├── CreateOrderRequest.java
│   │   │   │   ├── OrderItemRequest.java
│   │   │   │   ├── OrderItemResponse.java
│   │   │   │   ├── OrderResponse.java
│   │   │   │   └── UpdateOrderStatusRequest.java
│   │   │   ├── entity/
│   │   │   │   ├── Order.java
│   │   │   │   ├── OrderItem.java
│   │   │   │   └── OrderStatus.java
│   │   │   ├── exception/
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── InvalidOrderStateException.java
│   │   │   │   └── OrderNotFoundException.java
│   │   │   ├── factory/
│   │   │   │   └── OrderFactory.java
│   │   │   ├── mapper/
│   │   │   │   └── OrderMapper.java
│   │   │   ├── repository/
│   │   │   │   └── OrderRepository.java
│   │   │   ├── scheduler/
│   │   │   │   └── OrderProcessingScheduler.java
│   │   │   ├── service/
│   │   │   │   ├── OrderService.java
│   │   │   │   └── PaymentService.java
│   │   │   ├── strategy/
│   │   │   │   ├── OrderStatusTransitionStrategy.java
│   │   │   │   └── impl/
│   │   │   │       ├── CancelledStatusStrategy.java
│   │   │   │       ├── DeliveredStatusStrategy.java
│   │   │   │       ├── ProcessingStatusStrategy.java
│   │   │   │       └── ShippedStatusStrategy.java
│   │   │   └── OrderServiceApplication.java
│   │   └── resources/
│   │       └── application.yml
│   └── test/
│       └── java/com/ecommerce/orderservice/
│           ├── controller/
│           │   └── OrderControllerTest.java
│           └── service/
│               └── OrderServiceTest.java
├── build.gradle
├── settings.gradle
├── Dockerfile
├── entrypoint.sh
└── README.md
```

## 🚀 Getting Started

### Prerequisites

- Java 17 or higher
- Gradle 8.x
- Docker & Docker Compose (optional for containerized deployment)

### Local Development

1. **Clone the repository**
```bash
git clone <repository-url>
cd ecommerce-order-system
```

2. **Build the project**
```bash
./gradlew clean build
```

3. **Run the application**
```bash
./gradlew bootRun
```

The application will start on `http://localhost:8080`

### Database Configuration

The application supports two database configurations:

#### **Development (H2 - Default)**
Uses in-memory H2 database for local development and testing.

```bash
# Run with default profile (H2)
./gradlew bootRun
```

**Access H2 Console:**
```
URL: http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:orderdb
Username: sa
Password: (leave empty)
```

#### **Production (PostgreSQL)**
Uses PostgreSQL for production deployment.

```bash
# Run with production profile
./gradlew bootRun --args='--spring.profiles.active=prod'

# Or set environment variable
export SPRING_PROFILES_ACTIVE=prod
./gradlew bootRun
```

**Required Environment Variables for Production:**
```bash
DATABASE_URL=jdbc:postgresql://localhost:5432/orderdb
DB_USERNAME=postgres
DB_PASSWORD=your_password
PORT=8080  # Optional, defaults to 8080
```

**Key Differences:**
| Feature | Development (H2) | Production (PostgreSQL) |
|---------|-----------------|------------------------|
| Database | In-memory H2 | PostgreSQL |
| DDL Auto | create-drop | validate |
| SQL Logging | Enabled | Disabled |
| H2 Console | Enabled | Disabled |
| Error Details | Full details | Minimal details |
| Connection Pool | Default | Optimized (HikariCP) |

4. **Access Swagger UI** (API Documentation)
```
URL: http://localhost:8080/swagger-ui.html
OpenAPI Spec: http://localhost:8080/v3/api-docs
```

The Swagger UI provides:
- Interactive API documentation
- Try-it-out functionality for all endpoints
- Request/response schemas
- Example values


## 📚 API Documentation

### Swagger UI (Recommended)
Access the interactive API documentation at: **http://localhost:8080/swagger-ui.html**

The Swagger UI provides:
- Complete API documentation
- Try-it-out functionality for all endpoints
- Request/response schemas with examples
- Authentication support (if configured)

### API Endpoints

**Base URL:** `http://localhost:8080/api/orders`

**Context Path Structure:**
- Application root: `/`
- API prefix: `/api`
- Orders resource: `/api/orders`

### Endpoints

#### 1. Create Order
```http
POST /api/orders
Content-Type: application/json

{
  "items": [
    {
      "productId": 1,
      "quantity": 2,
      "price": 50.00
    },
    {
      "productId": 2,
      "quantity": 1,
      "price": 30.00
    }
  ]
}
```

**Response (201 Created):**
```json
{
  "timestamp": "2024-01-01T10:00:00",
  "status": 201,
  "message": "Order created successfully",
  "data": {
    "id": 1,
    "status": "PENDING",
    "items": [
      {
        "id": 1,
        "productId": 1,
        "quantity": 2,
        "price": 50.00,
        "lineTotal": 100.00
      }
    ],
    "totalAmount": 130.00,
    "createdAt": "2024-01-01T10:00:00",
    "updatedAt": "2024-01-01T10:00:00"
  }
}
```

#### 2. Get Order by ID
```http
GET /api/orders/{id}
```

**Response (200 OK):**
```json
{
  "timestamp": "2024-01-01T10:00:00",
  "status": 200,
  "message": "Order retrieved successfully",
  "data": {
    "id": 1,
    "status": "PENDING",
    "items": [...],
    "totalAmount": 130.00,
    "createdAt": "2024-01-01T10:00:00",
    "updatedAt": "2024-01-01T10:00:00"
  }
}
```

#### 3. List All Orders
```http
GET /api/orders
GET /api/orders?status=PENDING
```

**Response (200 OK):**
```json
{
  "timestamp": "2024-01-01T10:00:00",
  "status": 200,
  "message": "Orders retrieved successfully",
  "data": [
    {
      "id": 1,
      "status": "PENDING",
      "totalAmount": 130.00,
      ...
    }
  ]
}
```

#### 4. Update Order Status
```http
PUT /api/orders/{id}/status
Content-Type: application/json

{
  "status": "PROCESSING"
}
```

**Response (200 OK):**
```json
{
  "timestamp": "2024-01-01T10:00:00",
  "status": 200,
  "message": "Order status updated successfully",
  "data": {
    "id": 1,
    "status": "PROCESSING",
    ...
  }
}
```

#### 5. Cancel Order
```http
POST /api/orders/{id}/cancel
```

**Response (200 OK):**
```json
{
  "timestamp": "2024-01-01T10:00:00",
  "status": 200,
  "message": "Order cancelled successfully",
  "data": {
    "id": 1,
    "status": "CANCELLED",
    ...
  }
}
```

### Error Responses

#### Order Not Found (404)
```json
{
  "timestamp": "2024-01-01T10:00:00",
  "status": 404,
  "message": "Order Not Found",
  "error": "Order not found with id: 999"
}
```

#### Invalid State Transition (400)
```json
{
  "timestamp": "2024-01-01T10:00:00",
  "status": 400,
  "message": "Invalid Order State",
  "error": "Invalid state transition from PROCESSING to PENDING"
}
```

#### Validation Error (400)
```json
{
  "timestamp": "2024-01-01T10:00:00",
  "status": 400,
  "message": "Validation Failed",
  "data": {
    "items": "Order must contain at least one item",
    "price": "Price must be non-negative"
  }
}
```

#### Rate Limit Exceeded (429)
```json
{
  "timestamp": "2024-01-01T10:00:00",
  "status": 429,
  "message": "Too Many Requests",
  "error": "Rate limit exceeded. Please try again later."
}
```

#### Circuit Breaker Open (503)
```json
{
  "timestamp": "2024-01-01T10:00:00",
  "status": 503,
  "message": "Service Temporarily Unavailable",
  "error": "The service is currently unavailable. Please try again later."
}
```

## 🔄 Resilience Patterns

### Circuit Breaker Example

The payment service has a 30% failure rate to demonstrate circuit breaker behavior:

```java
@CircuitBreaker(name = "paymentService", fallbackMethod = "createOrderFallback")
public OrderResponse createOrder(CreateOrderRequest request) {
    // Payment processing with circuit breaker
}
```

**Testing Circuit Breaker:**
1. Create multiple orders rapidly
2. After ~50% failures, circuit opens
3. Subsequent requests fail fast with fallback response
4. After 10 seconds, circuit transitions to half-open
5. If successful, circuit closes

### Rate Limiter Example

```yaml
resilience4j:
  ratelimiter:
    instances:
      orderApi:
        limitForPeriod: 10
        limitRefreshPeriod: 1s
```

**Testing Rate Limiter:**
1. Send more than 10 requests per second
2. Requests beyond limit receive 429 status
3. Wait 1 second for limit refresh

## 🧪 Testing

### Run All Tests
```bash
./gradlew test
```

### Run Specific Test Class
```bash
./gradlew test --tests OrderServiceTest
./gradlew test --tests OrderControllerTest
```

### Test Coverage
- Unit tests for service layer (OrderServiceTest)
- Integration tests for controllers (OrderControllerTest)
- Mocking with Mockito
- MockMvc for REST endpoint testing

## 🐳 Docker Deployment

### Build Docker Image
```bash
./gradlew bootJar
docker build -t ecommerce-order-system:1.0.0 .
```


### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| SPRING_PROFILES_ACTIVE | default | Active Spring profile |
| SERVER_PORT | 8080 | Application port |
| DB_URL | jdbc:h2:mem:orderdb | Database URL |
| DB_USERNAME | sa | Database username |
| DB_PASSWORD | (empty) | Database password |

## 🤖 AI Usage & Development Notes

### Where AI (ChatGPT/Cursor) Was Used

1. **Initial Project Structure**
   - Generated Gradle build configuration
   - Created package structure following clean architecture

2. **Boilerplate Code Generation**
   - Entity classes with JPA annotations
   - DTO classes with validation annotations
   - Repository interfaces

3. **Design Pattern Implementation**
   - Strategy pattern structure for status transitions
   - Factory pattern for order creation
   - Exception handling framework

4. **Configuration Files**
   - application.yml with Resilience4j configuration
   - Dockerfile

5. **Test Cases**
   - Unit test structure and mock setup
   - Test data builders
   - MockMvc test scenarios

### Issues Faced & Solutions

#### Issue 1: Strategy Pattern Selection
**Problem:** Initially considered using a simple switch-case for status transitions, but this violated Open/Closed Principle.

**Solution:** Implemented Strategy Pattern with separate strategy classes for each status transition. This makes it easy to add new transitions without modifying existing code.

#### Issue 2: Circuit Breaker Not Triggering
**Problem:** Circuit breaker wasn't opening even with failures because the sliding window size was too large.

**Solution:** Adjusted configuration:
- Reduced `slidingWindowSize` from 100 to 10
- Set `minimumNumberOfCalls` to 5
- This made the circuit breaker more responsive to failures

#### Issue 3: Scheduler Running Too Frequently
**Problem:** Initial fixed delay caused scheduler to run immediately after completion, creating a tight loop.

**Solution:** Changed from `fixedDelay` to `fixedRate` with 5-minute interval, and made it configurable via application.yml.

### Trade-offs Made

1. **H2 vs PostgreSQL for Local Development**
   - **Decision:** Use H2 by default, PostgreSQL for production
   - **Trade-off:** H2 is easier for local development but PostgreSQL is production-ready
   - **Rationale:** Faster local setup, easy testing, production uses PostgreSQL via Docker

2. **Synchronous vs Asynchronous Order Processing**
   - **Decision:** Synchronous processing with scheduled batch updates
   - **Trade-off:** Simpler implementation vs better scalability
   - **Rationale:** Meets requirements, easier to understand and debug

3. **Manual Mapping vs MapStruct**
   - **Decision:** Manual mapping in OrderMapper
   - **Trade-off:** More code vs compile-time safety
   - **Rationale:** Simple mappings don't justify MapStruct complexity, but dependency is included for future use

4. **Simulated Payment Service**
   - **Decision:** Mock payment service with random failures
   - **Trade-off:** Not production-ready vs demonstrates circuit breaker
   - **Rationale:** Focus is on demonstrating resilience patterns, not payment integration

## 📝 Best Practices Implemented

1. **Clean Code**
   - Meaningful variable and method names
   - Single Responsibility Principle
   - DRY (Don't Repeat Yourself)

2. **SOLID Principles**
   - Single Responsibility: Each class has one reason to change
   - Open/Closed: Strategy pattern allows extension without modification
   - Liskov Substitution: All strategies are interchangeable
   - Interface Segregation: Focused interfaces
   - Dependency Inversion: Depend on abstractions

3. **RESTful API Design**
   - Proper HTTP methods (GET, POST, PUT)
   - Appropriate status codes (200, 201, 400, 404, 429, 503)
   - Consistent response structure
   - Resource-based URLs

4. **Error Handling**
   - Global exception handler
   - Custom exceptions for business logic
   - Meaningful error messages
   - Proper HTTP status codes

5. **Security Considerations**
   - Input validation with Bean Validation
   - Rate limiting to prevent abuse
   - Prepared statements (JPA) to prevent SQL injection

## 🔮 Future Enhancements

1. **Authentication & Authorization**
   - Spring Security with JWT
   - Role-based access control

2. **Event-Driven Architecture**
   - Kafka/RabbitMQ for order events
   - Async processing with message queues

3. **Observability**
   - Distributed tracing with Zipkin/Jaeger
   - Metrics with Micrometer/Prometheus
   - Centralized logging with ELK stack

4. **Advanced Caching**
   - Cache warming strategies
   - Distributed caching with Redis Cluster
   - Cache-aside pattern implementation


---

**Note:** This is a production-grade implementation demonstrating best practices in Spring Boot microservices development, including design patterns, resilience patterns, and clean architecture principles.