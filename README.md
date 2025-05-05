# Spring Boot Base Template

## Overview

This project serves as a robust foundation template (`Template Base`) for building new Spring Boot applications (WAR packaging). It comes pre-configured with essential enterprise features to accelerate development, including User Management with Role-Based Access Control (RBAC), comprehensive Auditing, and database-driven Job Scheduling using Quartz.

**Core Features:**

* **User Management & RBAC:** Secure user authentication (JWT-based) and authorization using Spring Security. Includes User and Role entities, services for CRUD operations, and validation logic. Default roles: `ROLE_ADMIN`, `ROLE_MANAGER`, `ROLE_SUPERVISOR`, `ROLE_OPERATOR`.
* **Auditing:** Aspect-Oriented Programming (AOP) based auditing using a custom `@Audit` annotation. Automatically logs method executions (including user, activity, type, request details, success/failure, duration, exceptions) to the `audit_trace` database table. Configurable audit levels and log retention. Includes an API for querying audit logs.
* **Job Scheduling (Quartz):** Integrates Quartz scheduler with JDBC Job Store for persistent job definitions. Jobs are defined in the `job_info` table and can be managed via a REST API (ADMIN only). Includes an example job (`AuditDeleteJob`) for cleaning up old audit logs. Jobs are initialized from `jobs.xml` on startup.
* **RESTful API Structure:** Includes controllers for Authentication (`/api/authorize`), User Management (`/api/user`), Audit Logs (`/api/audit`), and Job Management (`/api/job`).
* **Database Integration:** Configured with Spring Data JPA and Hibernate, using PostgreSQL as the default database. Includes base entities with audit fields (`creator`, `creationDate`, `lastModifiedBy`, `lastModification`).
* **Security:** Stateless JWT authentication, CSRF disabled, CORS configured (`CORSFilter`), method-level security using `@PreAuthorize`.
* **API Documentation:** Integrated Swagger UI using `springdoc-openapi` available at `/swagger-ui.html`.
* **Configuration:** Centralized configuration via `application.yml`.
* **Error Handling:** Centralized exception handling using `@ControllerAdvice`, custom exception classes, and `ErrorCode` enum. I18n support for error messages (`Messages.properties`).
* **Initialization:** Service (`InitializeServiceImpl`) to initialize roles and load initial users (`users.xml`) and jobs (`jobs.xml`) from a configurable path on application startup.
* **Utilities:** Helper classes for common tasks (Authorization, Date, String, Audit, Query). Paged results (`PagedResult`) for list APIs.

## Technology Stack

* **Java:** 21
* **Spring Boot:** 3.4.4 (or specify your exact version based on `pom.xml`)
* **Spring Framework:** Core, Web MVC, Security, Data JPA, AOP
* **Persistence:** Hibernate, Spring Data JPA
* **Database:** PostgreSQL (Configured by default)
* **Scheduling:** Quartz with JDBC Job Store
* **Security:** Spring Security, JWT (jjwt library)
* **API Documentation:** SpringDoc OpenAPI / Swagger UI
* **Build Tool:** Maven
* **Packaging:** WAR
* **Utilities:** Lombok, Apache Commons Lang3, Jackson Datatypes

## Prerequisites

Before you begin, ensure you have the following installed:

* JDK 21
* Maven
* A running instance of PostgreSQL.
* Git

## Getting Started

1.  **Clone the repository:**
    ```bash
    git clone <your-repository-url>
    cd base-template
    ```

2.  **Configure `application.yml`:**
    * Open `src/main/resources/application.yml`.
    * Update **Database Connection:** Modify `spring.datasource.url`, `username`, and `password` for your PostgreSQL instance.
    * Update **Initialization Path:** Set `app.initialize.file.path` to the directory containing `users.xml` and `jobs.xml` for initial data loading. Example: `/opt/base-tenant/resource/initialize/`. Ensure these files exist or remove/comment out the initialization logic in `InitializeServiceImpl` if not needed.
    * (Optional) Review and adjust other settings like `server.port`, `server.servlet.context-path`, `jwt.secret`, `app.audit.*`, etc.

3.  **Build the project:**
    ```bash
    ./mvnw clean install
    # or if mvnw is not present: mvn clean install
    ```

4.  **Run the application:**
    * **Using Maven:**
        ```bash
        ./mvnw spring-boot:run
        ```
    * **Deploying the WAR:** Copy the generated `target/base-template.war` (or your `finalName`) file to your servlet container's deployment directory (e.g., Tomcat's `webapps`).

The application should start, initialize roles/users/jobs (if configured), and be accessible at `http://localhost:<port><context-path>` (e.g., `http://localhost:8081/base-tenant`).

## Key Components Explained

### 1. User and Role Management (RBAC)

* **Entities:** `User`, `UserRole` (linked ManyToMany). `User` implements `UserDetails`.
* **Roles:** Defined in `RoleType` enum (`ROLE_ADMIN`, `ROLE_MANAGER`, `ROLE_SUPERVISOR`, `ROLE_OPERATOR`). Roles are initialized in the database on startup.
* **Security:** `SecurityConfig` sets up JWT authentication (`JwtUtils`, `AuthTokenFilter`). `UserDetailsServiceImpl` loads user data. `BCryptManager` handles password encoding.
* **API:** `UserController` (`/api/user/**`) provides endpoints for CRUD operations and password changes. `AuthorizeController` (`/api/authorize/**`) handles login (`/login`), retrieving current user info (`/who-am-i`), and profile updates. Access is controlled via `@PreAuthorize`.
* **Validation:** `UserValidator` enforces business rules (e.g., uniqueness, role assignment restrictions). Jakarta Validation constraints are used in `UserDTO`.

### 2. Auditing

* **Mechanism:** Uses Spring AOP (`AuditAspect`) triggered by the custom `@Audit` annotation on controller methods.
* **Data Captured:** Logs user actions, system events, request details (IP, User-Agent, URI, method, params, sanitized body), execution time, success/failure, and exception details into the `AuditTrace` entity.
* **Configuration:** Audit level (`app.audit.level`: `ALL`, `ERRORS_ONLY`, `SUCCESS_ONLY`, `NOTHING`, etc.) and log retention period (`app.audit.day`) are set in `application.yml`.
* **API:** `AuditController` (`/api/audit/**`) allows searching audit logs (requires `ROLE_ADMIN`).
* **Cleanup:** The `AuditDeleteJob` (if scheduled via `jobs.xml` or API) automatically deletes logs older than the configured retention period.

### 3. Job Scheduling (Quartz)

* **Persistence:** Uses `spring.quartz.job-store-type: jdbc` to store job details and triggers in the database (Quartz tables).
* **Job Definition:** Jobs are defined as records in the `JobInfo` entity/table, specifying the job class, group, name, description, schedule (CRON or simple interval), active status, and optional `jobDataMap` (as JSON).
* **Management:** `JobInfoService` handles the interaction between `JobInfo` entities and the Quartz scheduler. `JobController` (`/api/job/**`) provides ADMIN-only endpoints to list, schedule, update, pause, resume, trigger, and delete jobs.
* **Initialization:** Active jobs (`isActive=true`) from the `JobInfo` table are automatically scheduled on application startup. Initial job definitions can be loaded from `jobs.xml` via `InitializeServiceImpl`.
* **Dependency Injection:** Custom `AutoWiringSpringBeanJobFactory` allows injecting Spring beans into Quartz Job classes.

### 4. API Documentation (Swagger)

* Integrated using `springdoc-openapi-starter-webmvc-ui`.
* Access the UI at: `http://localhost:<port><context-path>/swagger-ui.html` (e.g., `http://localhost:8081/base-tenant/swagger-ui.html`)
* The UI documents all exposed REST endpoints and includes JWT authentication support (`Authorize` button).

### 5. Initialization Service

* `InitializeServiceImpl` runs at startup (`@PostConstruct`).
* Ensures all roles defined in `RoleType` exist in the `user_role` table.
* Loads initial users from `users.xml` (if found at `app.initialize.file.path`).
* Loads initial job definitions from `jobs.xml` (if found) into the `job_info` table.

## How to Use This Template for New Projects

1.  **Clone or Fork:** Create a new copy of this repository.
2.  **Rename Project:**
    * Update `groupId`, `artifactId`, `name`, `description`, and `finalName` in `pom.xml`.
    * Refactor the base package name (`it.andrea.start`) to your new project's package structure (e.g., `com.yourcompany.newproject`).
    * Update `server.servlet.context-path` in `application.yml`.
3.  **Customize Configuration (`application.yml`):** Adapt database settings, JWT secret, audit settings, ports, etc., for your new project.
4.  **Review Initialization:** Modify or remove `users.xml` and `jobs.xml` in the `app.initialize.file.path` directory according to your project's needs. Adjust `InitializeServiceImpl` if necessary.
5.  **Add Project-Specific Code:**
    * Define your application's domain entities (e.g., extending `FirstBaseEntity` or `SecondBaseEntity` for auditing/soft-delete).
    * Create corresponding Repositories, Services, DTOs, Mappers, and Controllers.
    * Leverage the existing User/Role management for securing your new endpoints using `@PreAuthorize`.
    * Add the `@Audit` annotation to your new controller methods where detailed logging is required.
    * Define custom jobs by creating classes extending `QuartzJobBean` and adding their definitions to the `job_info` table (via `jobs.xml`, migration script, or the API).
6.  **Review Security (`SecurityConfig.java`):** Adjust the `authorizeHttpRequests` rules to match your application's specific endpoint security requirements.
7.  **Develop & Test:** Build your application features on top of this foundation.

## API Endpoints

Key API groups are available under the context path (default `/base-tenant`):

* `/api/authorize`: Login, User Profile actions.
* `/api/user`: User CRUD operations (Admin/Manager restricted).
* `/api/audit`: Audit Log searching (Admin restricted).
* `/api/job`: Quartz Job management (Admin restricted).

Refer to the Swagger UI (`/swagger-ui.html`) for detailed endpoint documentation and testing.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
