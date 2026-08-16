# EOS Task Management API

Spring Boot REST API for a task-management application. The API provides authentication, role-based access control, task management, comments, notifications, and server-side pagination for the Angular frontend.

[Frontend application](../frontend)

## Features

- Login and registration with JWT authentication
- Database-driven roles and permissions
- `USER`, `ADMIN`, and `SUPER_ADMIN` access levels
- Task CRUD operations and status updates
- Task views for personal, assigned, and all tasks
- Server-side pagination, sorting, and filtering
- Task comments with ownership checks
- Notifications for task assignments and new comments
- Admin user management with protection for privileged accounts
- DTO validation and transactional database operations

## Architecture

The backend follows a simple layered structure:

- `controller` - REST endpoints and request validation
- `service` - business logic and transaction boundaries
- `repository` - Spring Data JPA database access
- `domain` - JPA entities mapped to the Oracle schema
- `dto` - objects exposed through the API
- `mapper` - conversion between entities and DTOs
- `config` - JWT filtering, CORS, Spring Security, and permission checks

The API does not trust ownership or role data sent by the client. The current user is taken from the authenticated JWT identity, then loaded from the database before permissions and ownership are checked.

## Security Model

- `/login` and `/register` are public endpoints.
- Other endpoints require an authenticated JWT.
- `PermissionChecker` loads the current user and permissions from the database.
- Administrators can manage tasks across users according to their permissions.
- Regular users can access only their assigned tasks and can update their task status.
- `SUPER_ADMIN` is required for operations involving administrator accounts.
- Task and comment ownership is checked in the service layer.

## Main Endpoints

| Area | Endpoints |
| --- | --- |
| Authentication | `POST /login`, `POST /register` |
| Tasks | `GET /tasks/page`, `GET /tasks/search`, `GET /tasks/{id}`, `POST /tasks`, `PUT /tasks/{id}`, `PATCH /tasks/{id}/status`, `DELETE /tasks/{id}` |
| Comments | `GET /tasks/{taskId}/comments`, `POST /tasks/{taskId}/comments`, `DELETE /tasks/{taskId}/comments/{commentId}` |
| Users | `GET /users/page`, `PATCH /users/{id}/role`, `DELETE /users/{id}` |
| Notifications | `GET /notifications`, `PATCH /notifications/{id}/read` |
| Statuses | `GET /statuses` |

### Task pagination

`GET /tasks/page` accepts these query parameters:

```text
page=0&size=8&sortBy=id&direction=asc&view=mine
```

Supported task views are `mine`, `assigned`, and `all`. Search filters are available through `GET /tasks/search` using `assignedTo`, `subject`, `dueDate`, and `status`.

## Technology Stack

- Java 21
- Spring Boot
- Spring Web MVC
- Spring Data JPA / Hibernate
- Spring Security
- Oracle Database
- Lombok
- jose4j for JWT processing
- Maven

## Database Configuration

The application expects an Oracle database with the schema already created. Hibernate runs with `ddl-auto=validate`, so it validates the entity mappings without changing the database schema.

Update `src/main/resources/application.properties` with the local Oracle connection details before starting the API.

## Running the Project

```bash
./mvnw spring-boot:run
```

The API runs on `http://localhost:8080` by default and is consumed by the Angular frontend on `http://localhost:4200`.

## Tests

```bash
./mvnw test
```
