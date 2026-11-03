# Configurable Approval Workflow Engine

A database-driven approval workflow engine built with Spring Boot and Spring Security. Designed for managing multi-step approval lifecycles for organizational requests such as Leave Requests and Expense Approvals without hardcoded business rules.

---

## Key Architecture & Core Principles

1. **Database-Driven Configuration**:
   - No hardcoded `if-else` or `switch-case` logic on request types or roles.
   - All approval chains, step sequences, and authorized roles are loaded from the `APPROVAL_STEPS` table.
   - Workflows can be updated or added dynamically in the database without recompiling or redeploying the backend.

2. **Security & Role-Based Access Control**:
   - Secured with JWT Bearer authentication.
   - Endpoint authorization enforced via Spring Security `@PreAuthorize` method security.
   - Three distinct enterprise roles:
     - `REQUESTER`: Submits requests and views personal request details/history.
     - `APPROVER`: Reviews and approves/rejects assigned steps in the workflow.
     - `ADMIN`: Overarching authority to override any step at any point.

3. **Core Workflow & State Machine Rules**:
   - **Self-Approval Prevention**: Requesters cannot approve or reject their own requests.
   - **Step-by-Step Sequence**: An approver can only act on the current pending step calculated from past audit history.
   - **Admin Override**: System administrators can act at any stage of the workflow.
   - **Transactional Integrity**: All status transitions (`PENDING` to `APPROVED` or `REJECTED`) and audit log entries are executed atomically within `@Transactional` boundaries.
   - **Immutable Audit History**: Every state change is recorded with the actor, action (`SUBMITTED`, `APPROVED`, `REJECTED`), and timestamp.

---

## Technology Stack

- **Java Version:** 25
- **Framework:** Spring Boot (Spring MVC, Spring Data JPA, Spring Security)
- **Database:** Embedded H2 Database (File-based persistence at `./data/approval_workflow_db`)
- **Security:** JWT Token Authentication & Method-level Security (`@PreAuthorize`)
- **Code Quality & Testing:** JUnit 5, Spring Boot Test, MockMvc, SonarQube Scanner

---

## Sample Users & Workflows

When the application boots up, `DataInitializer` automatically seeds:

### Users
| Name | Email | Password | Role |
|---|---|---|---|
| **om** | `om@test.com` | `password123` | `REQUESTER` |
| **sai** | `sai@test.com` | `password123` | `APPROVER` |
| **ram** | `ram@test.com` | `password123` | `ADMIN` |

### Workflows
1. **`LEAVE` Workflow:**
   - Step 1: Role `APPROVER`
   - Step 2: Role `ADMIN`
2. **`EXPENSE` Workflow:**
   - Step 1: Role `APPROVER`
   - Step 2: Role `ADMIN`

---

## Setup and Run Instructions

### 1. Run the Application
```bash
./mvnw spring-boot:run
```
*(On Windows: `.\mvnw.cmd spring-boot:run`)*

The server starts on port **8083**.

### 2. Access H2 Database Console
- URL: `http://localhost:8083/h2-console`
- JDBC URL: `jdbc:h2:file:./data/approval_workflow_db`
- User Name: `root`
- Password: `root`

### 3. Run Unit and Integration Tests
```bash
./mvnw test
```
*(On Windows: `.\mvnw.cmd test`)*

### 4. Run SonarQube Analysis
```bash
./mvnw clean verify sonar:sonar
```
*(On Windows: `.\mvnw.cmd clean verify sonar:sonar`)*

---

## API Request Flow

### 1. Login & Token Retrieval
`POST /auth/login`
```bash
curl -X POST http://localhost:8083/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "om@test.com",
    "password": "password123"
  }'
```

### 2. Create Request (`REQUESTER`)
`POST /requests`
```bash
curl -X POST http://localhost:8083/requests \
  -H "Authorization: Bearer <OM_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "LEAVE"
  }'
```

### 3. Fetch Request Details & Current Step (`REQUESTER`, `APPROVER`, `ADMIN`)
`GET /requests/1`
```bash
curl -X GET http://localhost:8083/requests/1 \
  -H "Authorization: Bearer <TOKEN>"
```

### 4. Approve Step 1 (`APPROVER`)
`POST /requests/1/approve`
```bash
curl -X POST http://localhost:8083/requests/1/approve \
  -H "Authorization: Bearer <SAI_TOKEN>"
```

### 5. Approve Step 2 / Final Step (`ADMIN`)
`POST /requests/1/approve`
```bash
curl -X POST http://localhost:8083/requests/1/approve \
  -H "Authorization: Bearer <RAM_TOKEN>"
```

### 6. Reject Request (`APPROVER`, `ADMIN`)
`POST /requests/{id}/reject`
```bash
curl -X POST http://localhost:8083/requests/2/reject \
  -H "Authorization: Bearer <SAI_TOKEN>"
```

### 7. View Immutable Audit History (`REQUESTER`, `ADMIN`)
`GET /requests/history/1`
```bash
curl -X GET http://localhost:8083/requests/history/1 \
  -H "Authorization: Bearer <OM_TOKEN>"
```

---

## Assumptions & Design Decisions

1. **Dynamic Step Calculation**: Current step position is derived by querying `APPROVAL_HISTORY` (`COUNT(action = 'APPROVED')`), avoiding race conditions with mutable counters.
2. **Stateless Security**: The engine uses stateless session management with JWT tokens.
3. **Database Seeding**: `DataInitializer` checks table counts before inserting default records, preventing duplicates across restarts.
4. **Error Handling**: Centralized error responses through `@RestControllerAdvice` (`GlobalExceptionHandler`).

---

## 5-Minute Loom Video Presentation Outline

Use this simple, step-by-step speaking script when recording your demo video:

### [00:00 - 00:45] Introduction
* **What to say:**
  > *"Hello everyone! My name is [Your Name]. Today I am presenting my submission for TechPulse IT Services: the Configurable Approval Workflow Engine built using Spring Boot and Spring Security.*
  > *The core goal of this project is to handle multi-step approval workflows like Leave and Expense requests completely through database configuration, with zero hardcoded `if-else` or `switch` statements."*
* **What to show on screen:** Open project in IDE, show the project structure and `README.md`.

---

### [00:45 - 01:45] Architecture & Database-Driven Design
* **What to say:**
  > *"Instead of hardcoding workflows in code, all steps and roles are stored in the database table `APPROVAL_STEPS`. If the company wants to add a 3-step workflow or change who approves what, they only change rows in the database—no code change or redeployment required.*
  > *For security, we implemented stateless JWT authentication with three distinct roles: `REQUESTER`, `APPROVER`, and `ADMIN`. We also enforce critical business rules: a requester cannot approve their own request, steps must be completed in order, and admins have override privileges."*
* **What to show on screen:** Open `H2 Console` in browser (`http://localhost:8083/h2-console`) and quickly show the `APPROVAL_STEPS` table.

---

### [01:45 - 03:30] Live API Demonstration (Postman)
* **What to say & do step-by-step:**
  1. **Login:** *"First, I log in as Om (`om@test.com`) who has the `REQUESTER` role. We receive a JWT token."*
  2. **Create Request:** *"Next, Om creates a new `LEAVE` request. The API returns status `PENDING`."*
  3. **Check Details:** *"Calling `GET /requests/{id}` shows the request is at Step 1, requiring an `APPROVER`."*
  4. **Self-Approval Prevention Rule:** *"If Om tries to approve his own request, the system rejects it with an error: 'You cannot approve or reject your own request'."*
  5. **Step 1 Approval:** *"Now I log in as Sai (`sai@test.com`, APPROVER) and call approve. Step 1 completes, and the request advances to Step 2 requiring `ADMIN`."*
  6. **Step 2 Final Approval:** *"Finally, Ram (`ram@test.com`, ADMIN) approves Step 2. Because all steps are complete, the request automatically transitions to `APPROVED`."*
  7. **Audit History:** *"Calling `GET /requests/history/{id}` shows the complete immutable audit trail with every action, actor, and timestamp."*
* **What to show on screen:** Execute the 5-6 Postman requests in sequence.

---

### [03:30 - 04:30] Automated Testing & SonarQube
* **What to say:**
  > *"Reliability is critical for workflow engines. We have written a comprehensive test suite using JUnit 5 and Spring Boot Test covering all API endpoints and integration workflows.*
  > *Let's run `mvn test` in the terminal..."*
  *(Run `.\mvnw.cmd test` and wait for it to complete)*
  > *"As you can see: 10 tests run, 0 failures, 0 errors, BUILD SUCCESS.*
  > *For code quality, we integrated `sonar-maven-plugin` directly in `pom.xml`, which allows any CI/CD pipeline or local SonarQube scanner to evaluate code quality, bugs, and security."*
* **What to show on screen:** Run `.\mvnw.cmd test` in terminal and show `BUILD SUCCESS`. Then show the plugin configuration in `pom.xml`.

---

### [04:30 - 05:00] Conclusion & Wrap-up
* **What to say:**
  > *"In summary, the project provides a clean, secure, transactional, and dynamic approval engine that fully meets all requirements of the TechPulse assignment. Thank you for your time, and I look forward to your feedback!"*
* **What to show on screen:** Return to IDE / GitHub repository page.
