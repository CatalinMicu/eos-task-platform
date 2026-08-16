# EOS Architecture Notes

## Request Flow

```text
Angular component
  -> Angular service
  -> auth interceptor adds Bearer JWT
  -> Spring controller
  -> service business logic and access checks
  -> mapper / repository
  -> Oracle database
```

The API returns DTOs instead of exposing JPA entities directly. The service layer resolves relationships such as the assigned user and status before the mapper creates the response DTO.

## Core Relationships

- One user can have many tasks.
- One task can have many comments.
- A comment belongs to one task and one user.
- A task can have many notifications.
- Roles and permissions are connected through `role_permissions`.

## Pagination

Task and user lists use backend pagination. The frontend sends the page, page size and sort parameters; Spring Data builds a `Pageable`, queries only the requested slice and returns the content together with the total element count.

## Authorization

The JWT filter validates the token and places the authenticated user id in Spring Security's context. `PermissionChecker` loads the current user and role permissions from the database. Service methods then combine role-level permissions with ownership checks where needed.
