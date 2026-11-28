if exceeds memory:   

-Xms128m -Xmx512m -XX:MaxMetaspaceSize=192m -XX:ReservedCodeCacheSize=64m -XX:+UseSerialGC

# FinLongevity – Microservices Setup

Workflow 1: Customer financial analysis

flowchart TD
A["Angular Financial Engine"] --> B["API Gateway"]
B --> C["JWT validation"]
C --> D["Financial Engine Service"]
D --> E["Customer Service Client"]
E --> F["Customer Service"]
F --> G["Profile, income, expenses, assets, liabilities"]
G --> H["Customer Snapshot Assembler"]
H --> I["Cash Flow Calculator"]
H --> J["Net Worth Calculator"]
H --> K["Financial Runway Calculator"]
I --> L["Longevity Score Calculator"]
J --> L
K --> L
L --> M["Persist Financial Analysis"]
M --> N["Analysis History and Dashboard"]




Workflow 2: Financial goal contribution and payment


sequenceDiagram
participant UI as Angular
participant G as Goal Service
participant P as Payment Service
participant K as Kafka
participant DB as Goal Database

UI->>G: Create contribution
G->>DB: Save PENDING contribution
G->>P: Create payment through REST
P-->>G: Payment ID
G->>DB: Save payment reference
UI->>P: Process mock payment
P->>P: Update payment status
P->>K: Publish payment status event
K->>G: Consume event
G->>DB: Lock contribution and goal
G->>DB: Confirm contribution and update goal




## Application Details

| Application              | Port | URL                                            | Purpose |
|--------------------------|-----:|------------------------------------------------|---|
| Eureka Discovery Server  | 8010 | http://localhost:8010                          | Eureka dashboard and service registry |
| Config Server            | 8012 | http://localhost:8012/identity-service/default | Serves Identity Service configuration |
| Identity Service         | 8080 | http://localhost:8080/health                   | Initial service health endpoint |
| Customer service         | 8081 | http://localhost:8081/actuator/health          | Initial service health endpoint  |
| API Gateway              | 8082 | http://localhost:8082/actuator/health          | API entry point and health endpoint |
| Provider Product service | 8083 | http://localhost:8083/actuator/health          | Initial service health endpoint  |
| Investment service       | 8084 | http://localhost:8083/actuator/health          | Initial service health endpoint  |
| Payment service          | 8085 | http://localhost:8083/actuator/health          | Initial service health endpoint  |
| Insurance service        | 8086 | http://localhost:8083/actuator/health          | Initial service health endpoint  |
| Goal service             | 8087 | http://localhost:8083/actuator/health          | Initial service health endpoint  |
| Financial engine service | 8088 | http://localhost:8083/actuator/health          | Initial service health endpoint  |


[9] registers on eureka discovery service


## SWAGGER

### Centralized Swagger – API Gateway
> **Note:** Ensure all required microservices are running and registered with Eureka before accessing centralized API documentation.
**Swagger UI:** http://localhost:8082/swagger-ui/index.html


### Individual Microservice Swagger URLs

| Microservice             | Port | Swagger UI                                  |
|--------------------------|-----:|---------------------------------------------|
| API GATEWAY              | 8082 | http://localhost:8082/swagger-ui/index.html |
| Identity Service         | 8080 | http://localhost:8080/swagger-ui/index.html |
| Customer Service         | 8081 | http://localhost:8081/swagger-ui/index.html |
| Provider Product Service | 8083 | http://localhost:8083/swagger-ui/index.html |
| Investment Service       | 8084 | http://localhost:8084/swagger-ui/index.html |
| Payment Service          | 8085 | http://localhost:8085/swagger-ui/index.html |


## Start-up Order

Start the applications in the following order:

1. **Discovery Server** — http://localhost:8010
2. **Config Server** — http://localhost:8012
3. **Identity Service** — http://localhost:8080
4. **API Gateway** — http://localhost:8082
5. **Customer Service** — http://localhost:8081

## Identity-service
In this service we added database-backed customer registration, BCrypt password hashing, login, JWT access-token generation, and a protected current-user endpoint.

## Endpoints

Direct Identity Service (port 8080):

- `POST http://localhost:8080/auth/register`
- `POST http://localhost:8080/auth/login`
- `GET http://localhost:8080/users/me` (requires Bearer token)

Through API Gateway (port 8082):

- `POST http://localhost:8082/api/identity/auth/register`
- `POST http://localhost:8082/api/identity/auth/login`
- `GET http://localhost:8082/api/identity/users/me` (requires Bearer token)

## Register request

```json
{
  "fullName": "Demo Customer",
  "email": "demo.customer@example.com",
  "password": "StrongPass123"
}
```

Expected: HTTP 201 Created, with user ID, name, email, role and creation time. The password is never returned.

## Login request

```json
{
  "email": "demo.customer@example.com",
  "password": "StrongPass123"
}
```

Expected: HTTP 200 with `accessToken`, `tokenType`, `expiresIn`, and safe user details.

# FinLongevity – Authentication & Security Flow

This document explains the authentication and security workflow implemented in FinLongevity, covering user registration, login, JWT generation, token validation and protected API access.

## 1. User Registration Flow

A new customer can create an account using their full name, email and password.

### Registration Architecture

```mermaid
flowchart TD
    A["Angular Frontend<br/>Full Name, Email, Password"]
    B["API Gateway<br/>POST /api/identity/auth/register"]
    C["AuthController"]
    D["AuthService.register()"]
    E["Check Existing Email"]
    F["PasswordEncoder<br/>BCrypt Hashing"]
    G["UserRepository"]
    H[("MySQL Database")]
    I["UserResponse<br/>Safe User Information"]

    A --> B
    B --> C
    C --> D
    D --> E
    E --> F
    F --> G
    G --> H
    H --> I
```

### Registration Steps

1. Customer enters full name, email and password in the Angular frontend.
2. API Gateway forwards the registration request to Identity Service.
3. `AuthController` invokes `AuthService.register()`.
4. `AuthService` normalizes the email and checks whether the account already exists.
5. `PasswordEncoder` hashes the password using BCrypt.
6. `UserRepository` saves the new customer in MySQL.
7. `UserResponse` returns safe user information without exposing the password.

**Endpoint:** `POST /api/identity/auth/register`

---

## 2. User Login Flow

After registration, the customer can log in using their email and password.

### Login Architecture

```mermaid
flowchart TD
    A["Angular Frontend<br/>Email and Password"]
    B["API Gateway<br/>POST /api/identity/auth/login"]
    C["AuthController"]
    D["AuthService.login()"]
    E["AuthenticationManager"]
    F["CustomUserDetailsService"]
    G[("MySQL Database")]
    H["PasswordEncoder<br/>BCrypt Password Verification"]
    I{"Credentials Valid?"}
    J["JwtService<br/>Generate Signed JWT"]
    K["AuthResponse<br/>Access Token and User Details"]
    L["401 Unauthorized"]

    A --> B
    B --> C
    C --> D
    D --> E
    E --> F
    F --> G
    G --> H
    H --> I
    I -->|Yes| J
    J --> K
    I -->|No| L
```

### Login Steps

1. Customer submits their email and password.
2. API Gateway forwards the request to Identity Service.
3. `AuthService.login()` invokes `AuthenticationManager` to authenticate the credentials.
4. `CustomUserDetailsService` retrieves the customer details from MySQL.
5. `PasswordEncoder` verifies the submitted password against the stored BCrypt hash.
6. If authentication succeeds, `JwtService` generates a signed JWT containing the user's identity and role.
7. `AuthResponse` returns the access token, token type, expiration time and user details.

**Endpoint:** `POST /api/identity/auth/login`

---

## 3. Protected API Access Flow

After successful login, the customer receives a JWT and uses it to access protected APIs.

FinLongevity follows a centralized JWT validation approach in which the API Gateway validates incoming tokens before forwarding requests to downstream microservices.

### Authentication Architecture

```mermaid
flowchart TD
    A["Angular Frontend<br/>Authorization: Bearer JWT"]
    B["API Gateway<br/>JwtGatewayFilter"]
    C{"JWT Valid?"}
    D["401 Unauthorized"]
    E["Extract JWT Claims<br/>User ID, Email, Role"]
    F["Remove Untrusted Identity Headers<br/>Add Trusted Identity Headers"]
    G["Identity Service<br/>GatewayHeaderAuthenticationFilter"]
    H["CustomUserDetailsService<br/>Load User from MySQL"]
    I{"User Exists and ID Matches?"}
    J["401 Unauthorized"]
    K["Spring SecurityContext<br/>Set Authenticated Principal and Authorities"]
    L["UserController<br/>GET /users/me"]
    M["Return Current User Information"]

    A --> B
    B --> C
    C -->|No| D
    C -->|Yes| E
    E --> F
    F --> G
    G --> H
    H --> I
    I -->|No| J
    I -->|Yes| K
    K --> L
    L --> M
```

### Protected API Access Steps

1. Angular sends the JWT in the `Authorization: Bearer` header.
2. `JwtGatewayFilter` validates the JWT signature, expiration and required claims.
3. If the token is valid, the Gateway extracts the user ID, email and role from the JWT.
4. Gateway removes caller-supplied identity headers and adds trusted identity headers.
5. `GatewayHeaderAuthenticationFilter` receives the request inside Identity Service.
6. `CustomUserDetailsService` loads the user from MySQL.
7. Identity Service verifies that the user ID from the Gateway matches the database user.
8. `SecurityContextHolder` stores the authenticated principal and authorities.
9. `UserController` processes the protected request and returns the current user's information.

**Example endpoint:** `GET /api/identity/users/me`

**Authorization:** Bearer Token Required

---

## 4. Trusted Identity Headers

After successful JWT validation, the API Gateway forwards authenticated user information to downstream services using trusted HTTP headers.

| Header | Example Value | Description |
|---|---|---|
| `X-Authenticated-User-Id` | `101` | Authenticated user's ID |
| `X-Authenticated-User-Email` | `rahul@gmail.com` | Authenticated user's email |
| `X-Authenticated-User-Role` | `CUSTOMER` | Authenticated user's role |

The Gateway removes any identity headers supplied by the original client before inserting validated values.

---

## 5. Security Components and Responsibilities

| Component | Responsibility |
|---|---|
| `AuthController` | Exposes registration and login endpoints |
| `AuthService` | Handles registration and login business logic |
| `AuthenticationManager` | Authenticates user credentials |
| `CustomUserDetailsService` | Loads user details from MySQL |
| `PasswordEncoder` | Performs BCrypt password hashing and verification |
| `JwtService` | Generates signed JWT tokens |
| `JwtGatewayFilter` | Validates JWT tokens at API Gateway |
| `GatewayHeaderAuthenticationFilter` | Establishes the authenticated principal using trusted Gateway headers |
| `SecurityConfig` | Configures endpoint access and Spring Security |
| `SecurityContextHolder` | Stores the current request's authentication information |

---

## 6. Authentication and Authorization Responses

| Scenario | HTTP Status | Description |
|---|---:|---|
| Successful registration | 200 OK | Customer account created |
| Duplicate email | 409 Conflict | Account already exists |
| Successful login | 200 OK | JWT generated |
| Invalid login credentials | 401 Unauthorized | Authentication failed |
| Missing JWT on protected endpoint | 401 Unauthorized | Authentication required |
| Invalid or expired JWT | 401 Unauthorized | Token rejected |
| User does not exist | 401 Unauthorized | Identity validation failed |
| Insufficient permissions | 403 Forbidden | Authenticated user lacks required authority |

---

## 7. Overall Security Architecture

FinLongevity separates authentication responsibilities across its microservices architecture:

- **Identity Service:** Manages customer registration, login, password security and JWT generation.
- **API Gateway:** Acts as the centralized entry point and validates JWT tokens for protected requests.
- **Gateway Identity Propagation:** Passes validated user identity to downstream services using trusted headers.
- **Spring Security:** Establishes the authenticated principal and enforces endpoint-level authorization.

**Design Approach:** Centralized JWT validation at the API Gateway with service-level Spring Security authentication and authorization.



# Customer Service – Authentication & Profile Management

## Overview

Customer Service is responsible for creating and managing customer profiles for users registered through Identity Service.

It does not perform independent user authentication, JWT validation, or identity verification. These responsibilities belong to Identity Service and API Gateway.

Customer Service trusts the authenticated identity context forwarded by API Gateway through HTTP headers.

## Authentication Architecture

```text
                 CLIENT
                   |
                   | Request with JWT
                   v
              API GATEWAY
                   |
                   | 1. Extract Bearer Token
                   | 2. Validate JWT Signature
                   | 3. Validate Token Expiration
                   | 4. Extract User Claims
                   |
                   | Forward Trusted Headers
                   |
                   | X-Authenticated-User-Id
                   | X-Authenticated-User-Email
                   | X-Authenticated-User-Role
                   v
             CUSTOMER SERVICE
                   |
                   | GatewayAuthenticationFilter
                   | 1. Read Identity Headers
                   | 2. Check Required Headers
                   | 3. Create Security Principal
                   | 4. Establish Security Context
                   |
                   v
             CUSTOMER PROFILE
                   |
                   | Create / Retrieve / Update
                   v
          FINANCIAL_CUSTOMER_DB
```

## Responsibilities

### 1. Identity Service

- Handles user registration and login.
- Validates user credentials.
- Stores user identity information.
- Generates JWT tokens after successful authentication.

### 2. API Gateway

- Acts as the entry point for client requests.
- Validates JWT signatures and expiration.
- Extracts authenticated user information from JWT claims.
- Removes untrusted identity headers supplied by clients.
- Adds trusted identity headers before forwarding requests to downstream services.

### 3. Customer Service

- Receives authenticated identity information from API Gateway.
- Does not validate JWT tokens independently.
- Does not access the Identity Service database.
- Creates customer profiles for authenticated identity users.
- Uses the authenticated user ID to associate a customer profile with its identity account.
- Manages customer profile information independently.

## Customer Profile Creation Flow

```text
1. User registers through Identity Service.
                |
                v
2. Identity Service creates the user account.
                |
                v
3. User logs in and receives JWT.
                |
                v
4. Client sends a profile creation request
   through API Gateway with the JWT.
                |
                v
5. API Gateway validates the JWT.
                |
                v
6. Gateway extracts userId, email and role.
                |
                v
7. Gateway forwards authenticated identity headers.
                |
                v
8. Customer Service GatewayAuthenticationFilter
   reads the identity headers.
                |
                v
9. SecurityContext is established.
                |
                v
10. Customer Service creates the customer profile
    using the authenticated user ID.
                |
                v
11. Profile is saved in financial_customer_db.
                |
                v
12. Successful response returned to the client.
```

## Identity Headers

API Gateway forwards the following headers:

| Header | Purpose |
|---|---|
| `X-Authenticated-User-Id` | Identifies the authenticated user |
| `X-Authenticated-User-Email` | Provides the user's email |
| `X-Authenticated-User-Role` | Provides the user's assigned role |

Customer Service uses these headers to establish the local Spring Security principal.

## Database Ownership

| Service | Database | Responsibility |
|---|---|---|
| Identity Service | `financial_identity_db` | User accounts, credentials and roles |
| Customer Service | `financial_customer_db` | Customer profiles and customer-related information |

The customer profile references the identity account using its user ID. Customer Service does not maintain a direct database relationship with Identity Service.

## Security Design

Customer Service follows a Gateway-based authentication model.

- JWT validation is centralized at API Gateway.
- Identity Service remains the owner of user accounts.
- Customer Service trusts identity headers inserted by the Gateway.
- Customer Service creates its own local SecurityContext.
- Missing authentication headers result in HTTP 401 Unauthorized.
- Customer Service is not intended to be directly accessible by external clients.

**Important:** Direct access to Customer Service must be restricted to trusted internal traffic. Otherwise, an external caller could forge identity headers and impersonate another user.

## Design Principle

**Identity Service establishes who the user is. API Gateway authenticates incoming requests. Customer Service manages the user's customer profile.**

This separation maintains clear service responsibilities and independent database ownership within the FinLongevity microservices architecture.

## now
Now implementing: payment modes for customer:
can have one full time employment and may have multiple side earning business


## Employment
- `PUT /api/customers/me/employment` — create or update one current employment record.
- `GET /api/customers/me/employment` — retrieve it.

Example:
```json
{
  "employmentType": "FULL_TIME",
  "employerName": "Example Technologies",
  "designation": "Software Engineer",
  "employmentStartDate": "2025-06-27",
  "employmentEndDate": null,
  "currentlyEmployed": true
}
```

## Income sources
- `POST /api/customers/me/income-sources` — add a source.
- `GET /api/customers/me/income-sources` — list the signed-in user's sources.
- `PUT /api/customers/me/income-sources/{id}` — update an owned source.
- `DELETE /api/customers/me/income-sources/{id}` — delete an owned source.

Example:
```json
{
  "incomeType": "SALARY",
  "sourceName": "Monthly Salary",
  "amount": 85000.00,
  "frequency": "MONTHLY",
  "currency": "INR",
  "active": true
}
```

now customer can add assets and his liabilities

now customer can add nominies
- Each nominee belongs to one authenticated identity user (identityUserId).

## Risk Tolerance :
"What kind of financial products and investment options are suitable for this customer's preferences and goals?"

customer declared: risk profile

This profile stores customer-declared preferences. It is not a regulated suitability assessment or a personalized investment recommendation.

This tells us about:Customer A – Conservative
Low risk preference
- Beginner investor
- Prefers Fixed Deposits and Bonds
- Objective: Capital preservation
- FinLongevity can prioritise showing lower-risk product options.


Customer B – Moderate
Medium risk preference
- Intermediate investor
- Prefers Mutual Funds and FDs
- Objective: Wealth creation
- FinLongevity can show a mix of product categories for comparison.


Customer C – Aggressive
Higher risk preference
- Experienced investor
- Interested in Stocks and Mutual Funds
- Objective: Long-term wealth creation
- FinLongevity can highlight products matching those declared preferences.

## Household & Family Members
This module currently manages the household owner's household and dependant records.


## Investment service
Its responsibility will be to manage customer investment records, holdings, investment transactions and redemption records. 

It will reference products using their IDs from Provider & Product Service rather than storing or managing the product catalogue itself.

## Payment service
payment : created -> initiated 
then either success or fails

## kafka
When a user contributes ₹5,000 to a goal:
1. Goal Service saves the contribution.
2. Payment Client sends a request to Payment Service to create a payment of ₹5,000.
3. Payment Service creates the payment and returns a Payment ID.
4. Payment Client gives that Payment ID back to Goal Service.
5. Goal Service stores the Payment ID against the contribution.


# Financiall engine

Module -	What it calculates

Cash Flow Analyzer - Income minus expenses

Net Worth Analyzer - Assets minus liabilities

Emergency Fund Analyzer - Months of expense coverage

Financial Runway - How long savings can sustain expenses

Savings & Investment Analyzer - Savings rate and investment growth

Retirement Projection - Projected retirement corpus

Goal Feasibility - Whether goals are financially achievable

Longevity Scoring - Overall financial sustainability score

Scenario Simulator - Future financial outcomes



### Financial Longevity Engine

Customer Financial Data
[ Income · Expenses · Assets · Liabilities · Risk Profile · Goals ]


Financial Longevity Engine
[ Cash Flow · Net Worth · Sustainability · Forecasting · Risk Analysis ]


Longevity Score
[ Financial health indicator ]


Retirement Forecast
[ Future wealth projection ]


Financial Runway
[ Months of expense coverage ]


Scenario Analysis
[ Best, expected and worst cases ]



Calculations implemented
1. Monthly surplus = Income − Expenses − Debt Payments
2. Savings rate = (Monthly Surplus / Monthly Income) × 100
3. Net worth = Total Assets − Total Liabilities
4. Emergency fund coverage = Liquid Assets / Monthly Expenses
5. Financial runway = Liquid Assets / (Monthly Expenses + Debt Payments)


## Longevity scoring model
Maximum score
100

Savings rate
30 points


Emergency fund
30 points


Debt payment burden
20 points


Positive net worth
20 points

Health bands:
Score	Financial health
0–24	Critical
25–44	Needs improvement
45–64	Fair
65–84	Good
85–100	Excellent




Install
Merge the dto, engine, service and exception packages into the existing project.

Score model (100 points)
Savings rate: 0–30 points (<=0: 0; 0–10%: 10; 10–20%: 20; >=20%: 30).
Emergency fund: 0–30 points (<1 month: 5; 1–3: 15; 3–6: 24; >=6: 30; absent/zero: 0).
Debt payment burden: 0–20 points (<=10% of income: 20; <=20%: 15; <=35%: 8; >35%: 0; income <=0: 0).
Positive net worth: 20 points; zero/negative: 0.
Health bands:

0–24 CRITICAL
25–44 NEEDS_IMPROVEMENT
45–64 FAIR
65–84 GOOD
85–100 EXCELLENT



## Retirement calculation assumptions
- Annual expected return is treated as a nominal annual rate divided by 12 for monthly compounding.
- Monthly contributions are added at each month-end after that month's growth.
- Contribution amount increases by the configured annual growth rate after each projected year.
- Retirement expense inflation is applied once per projected year.
- Required retirement corpus uses an illustrative 25x annual-expense heuristic. This is not a guarantee or personalized retirement recommendation.
- The `yearsToProject` should normally equal `retirementAge - currentAge`; the service layer in a later layer should validate this consistency.
