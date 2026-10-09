# Personal Finance Manager REST API

A Spring Boot 3 RESTful web application for managing personal finances, tracking income/expenses, defining custom categories, creating savings goals, and generating monthly and yearly analytics reports.

## Tech Stack
- **Language**: Java 17
- **Framework**: Spring Boot 3.2.5
- **Security**: Spring Security (session-based auth with JSESSIONID cookies)
- **Database**: H2 In-Memory Database
- **Build Tool**: Maven
- **Testing**: JUnit 5, Mockito, Spring Boot Test, JaCoCo (coverage)

## Architecture
- **Layered Architecture**: Controller → Service → Repository → Database
- **DTO Separation**: Clean separation between Request/Response DTOs and JPA entities
- **Exception Handling**: Global exception handler (`@RestControllerAdvice`) producing consistent JSON error responses
- **Security**: BCrypt password hashing and per-user data isolation
- **JavaDoc**: All public classes and public methods are documented

## Getting Started

### Prerequisites
- JDK 17 or higher
- Maven 3.8+ (or Maven binary)

### Run Application
Execute the following Maven command from the project root:

```bash
mvn spring-boot:run
```

The server will start at `http://localhost:8080`.

H2 Database Console is available at: `http://localhost:8080/h2-console`
- **JDBC URL**: `jdbc:h2:mem:financedb`
- **Username**: `sa`
- **Password**: *(leave blank)*

### Run Tests
```bash
mvn test
```

Unit and controller tests are located in `src/test`. JaCoCo generates the coverage report in `target/site/jacoco/index.html`.

## Design Decisions
- **Session-based authentication**: Login stores the Spring Security context in the HTTP session (JSESSIONID cookie, HTTP-only, 30-minute timeout). All `/api/**` endpoints except `/api/auth/register` and `/api/auth/login` require an authenticated session; unauthenticated requests receive HTTP 401 with a JSON body.
- **Data isolation**: Every repository query is scoped by the owning user. Resources that exist but belong to another user are answered with HTTP 403 (Forbidden); resources that do not exist are answered with HTTP 404 (Not Found).
- **404 vs 403 distinction**: Update/delete operations first load the resource by id, then verify ownership. This allows the API to return 404 for unknown ids and 403 for foreign-owned resources, as specified in the assignment's error table.
- **Category model**: Default categories (Salary; Food, Rent, Transportation, Entertainment, Healthcare, Utilities) are shared rows with a `null` owner and cannot be modified or deleted. Custom categories are owned by exactly one user; names are unique per user (case-insensitive) and may not collide with default names. Deleting another user's custom category returns 403; deleting a referenced category returns 400.
- **Immutable transaction date**: The transaction date cannot be changed after creation; `PUT /api/transactions/{id}` rejects requests that attempt to modify it (HTTP 400).
- **Goal progress**: Progress is computed on the fly as (total income − total expenses) since the goal start date, so deleted transactions are automatically reflected in goals and reports.
- **Validation on write operations**: `@Valid` is applied to all request bodies, including updates, so amount/date constraints are enforced on every write.
- **Report input validation**: Month must be 1–12 and year 1–9999; invalid values return HTTP 400 instead of a 500 error.
- **CSRF disabled & H2 console exposed**: Deliberate trade-offs for a stateless demo API tested with plain HTTP clients and for using the H2 web console. Not recommended for production without re-enabling CSRF protection and restricting the console.
- **In-memory database**: H2 is used as permitted by the assignment; note that all data resets on restart.

## Input Validation & Policies
- **Username**: must be a valid email address, unique.
- **Password**: 8–100 characters (BCrypt-hashed at rest).
- **Phone number**: optional leading `+`, 7–18 characters of digits, spaces, dashes or parentheses (e.g. `+1234567890`).
- **Amounts**: positive decimal values (minimum 0.01).
- **Transaction date**: `YYYY-MM-DD`, not in the future; immutable after creation.
- **Goal target date**: must be in the future.

## Error Responses
All errors return a JSON body of the form `{"error": "...", "message": "..."}`.

| Status | Meaning |
|---|---|
| 200/201 | Success |
| 400 | Validation errors, malformed input, default-category deletion, referenced-category deletion, immutable-field modification |
| 401 | Invalid credentials, expired/missing session |
| 403 | Accessing another user's data |
| 404 | Resource not found |
| 409 | Duplicate username or duplicate category name |

Known scenarios always produce 4xx responses; unexpected server errors are not expected for valid input.

---

## API Documentation

### 1. User Management & Authentication

#### Register User
`POST /api/auth/register`
```json
{
  "username": "user@example.com",
  "password": "password123",
  "fullName": "John Doe",
  "phoneNumber": "+1234567890"
}
```
**Response (201 Created)**: `{ "message": "User registered successfully", "userId": 1 }`

#### Login User
`POST /api/auth/login`
```json
{
  "username": "user@example.com",
  "password": "password123"
}
```
**Response (200 OK)**: `{ "message": "Login successful" }`
Sets the `JSESSIONID` session cookie for subsequent API calls.

#### Logout User
`POST /api/auth/logout` (uses session cookie)

**Response (200 OK)**: `{ "message": "Logout successful" }`

---

### 2. Category Management

#### Get All Categories
`GET /api/categories`

**Response (200 OK)**:
```json
{
  "categories": [
    { "name": "Salary", "type": "INCOME", "isCustom": false },
    { "name": "Food", "type": "EXPENSE", "isCustom": false },
    { "name": "CustomCategory", "type": "EXPENSE", "isCustom": true }
  ]
}
```

#### Create Custom Category
`POST /api/categories`
```json
{ "name": "SideBusinessIncome", "type": "INCOME" }
```
**Response (201 Created)**: `{ "name": "SideBusinessIncome", "type": "INCOME", "isCustom": true }`

#### Delete Custom Category
`DELETE /api/categories/{name}`

**Response (200 OK)**: `{ "message": "Category deleted successfully" }`

---

### 3. Transaction Management

#### Create Transaction
`POST /api/transactions`
```json
{
  "amount": 50000.00,
  "date": "2024-01-15",
  "category": "Salary",
  "description": "January Salary"
}
```
**Response (201 Created)**:
```json
{ "id": 1, "amount": 50000.00, "date": "2024-01-15", "category": "Salary", "description": "January Salary", "type": "INCOME" }
```

#### Get Transactions (with filtering)
`GET /api/transactions?startDate=2024-01-01&endDate=2024-01-31&categoryId=1&type=INCOME`

Results are sorted by newest first.

#### Update Transaction
`PUT /api/transactions/{id}`
```json
{ "amount": 60000.00, "description": "Updated January Salary" }
```
The `date` field is immutable and cannot be modified.

#### Delete Transaction
`DELETE /api/transactions/{id}`

**Response (200 OK)**: `{ "message": "Transaction deleted successfully" }`

---

### 4. Savings Goals

#### Create Savings Goal
`POST /api/goals`
```json
{
  "goalName": "Emergency Fund",
  "targetAmount": 5000.00,
  "targetDate": "2026-01-01",
  "startDate": "2025-01-01"
}
```
`startDate` is optional and defaults to the creation date.

**Response (201 Created)**:
```json
{
  "id": 1, "goalName": "Emergency Fund", "targetAmount": 5000.00,
  "targetDate": "2026-01-01", "startDate": "2025-01-01",
  "currentProgress": 1000.00, "progressPercentage": 20.0, "remainingAmount": 4000.00
}
```

#### Get All Savings Goals
`GET /api/goals`

#### Get Savings Goal by ID
`GET /api/goals/{id}`

#### Update Savings Goal
`PUT /api/goals/{id}`
```json
{ "targetAmount": 6000.00, "targetDate": "2026-02-01" }
```

#### Delete Savings Goal
`DELETE /api/goals/{id}`

**Response (200 OK)**: `{ "message": "Goal deleted successfully" }`

---

### 5. Reports & Analytics

#### Monthly Financial Report
`GET /api/reports/monthly/{year}/{month}`

**Response (200 OK)**:
```json
{
  "month": 1, "year": 2024,
  "totalIncome": { "Salary": 3000.00, "Freelance": 500.00 },
  "totalExpenses": { "Food": 400.00, "Rent": 1200.00, "Transportation": 200.00 },
  "netSavings": 1700.00
}
```

#### Yearly Financial Report
`GET /api/reports/yearly/{year}`

**Response (200 OK)**:
```json
{
  "year": 2024,
  "totalIncome": { "Salary": 36000.00, "Freelance": 6000.00 },
  "totalExpenses": { "Food": 4800.00, "Rent": 14400.00, "Transportation": 2400.00 },
  "netSavings": 20400.00
}
```

## Deployment
The application can be deployed to Render (or a similar free hosting service) as a Java/Maven web service. The API base URL is served under `/api` (e.g. `https://<your-service>.onrender.com/api`). Note that the H2 in-memory database resets on each restart.
