# Advanced Query System Guide

## Overview

The new query system provides **full-featured filtering, sorting, searching, and pagination** for all entities. The field operation control utility has been **removed** as it was unnecessary complexity.

## Query Parameters

All list endpoints now support these query parameters:

### Pagination
- `page` - Page number (0-indexed, default: 0)
- `size` - Page size (default: 20)

### Sorting
- `sort` - Sort fields: `field1:asc,field2:desc` or `field1,field2` (default asc)
  - Examples:
    - `sort=name:asc,created_at:desc`
    - `sort=email` (defaults to asc)

### Searching
- `search` - Full-text search across multiple fields (entity-specific)
  - Example: `search=john`

### Filtering
- `filter` - Filter conditions: `field1:operator:value1,field2:value2`
  - Operators: `eq`, `ne`, `gt`, `gte`, `lt`, `lte`, `like`, `in`, `between`
  - Default operator is `eq` if not specified
  - Examples:
    - `filter=statusId:eq:1` - Status ID equals 1
    - `filter=teamId:2` - Team ID equals 2 (default eq)
    - `filter=created_at:gte:2024-01-01` - Created after date
    - `filter=name:like:john` - Name contains "john"
    - `filter=roleId:in:1|2|3` - Role ID in [1,2,3]
    - `filter=created_at:between:2024-01-01:2024-12-31` - Date range

### Field Selection (Future)
- `fields` - Select only specific fields (not yet implemented)

## Examples

### Employees
```
GET /api/v1/employees?page=0&size=10&sort=name:asc&search=john&filter=teamId:1
```

### Tickets
```
GET /api/v1/tickets?page=0&size=20&sort=created_at:desc&filter=statusId:1,projectId:5
```

### Projects
```
GET /api/v1/projects?sort=projectName:asc&filter=teamId:2
```

## Response Format

All list endpoints return paginated response:

```json
{
  "statusCode": 200,
  "message": "Success",
  "data": {
    "content": [...],
    "page": 0,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5,
    "first": true,
    "last": false
  },
  "timestamp": "2024-01-01T00:00:00Z"
}
```

## Why Field Operation Control Was Removed

The `FieldOperationControl` and `ApiFieldControlService` were:
- **Not being used** - No controllers/services actually called them
- **Over-engineered** - Added complexity without benefit
- **Redundant** - Validation is already handled by:
  - Jakarta Validation annotations
  - Service-level validation
  - Entity constraints

If you need field-level access control in the future, implement it at the **security/authorization layer**, not as a separate utility.
