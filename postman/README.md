# Postman Collection for DailyStandUp API

## Quick Start

1. **Import Collection & Environment**
   - Open Postman
   - Click "Import" button
   - Import both files:
     - `DailyStandUp.postman_collection.json`
     - `DailyStandUp.postman_environment.json`

2. **Select Environment**
   - In Postman, select "DailyStandUp Environment" from the environment dropdown (top right)

3. **Update Base URL** (if needed)
   - If your server is not running on `http://localhost:8080`, update the `base_url` variable in the environment

4. **Run Setup Flow**
   - Open folder "1. Setup & Authentication"
   - Run requests in order:
     1. Create Default Statuses (To Do, In Progress, Completed)
     2. Create Roles (Admin, Manager, User)
     3. Create Team
     4. Signup - Create Admin User
     5. Login (to get JWT token)

5. **Test Other Endpoints**
   - All other folders contain organized API endpoints
   - JWT token is automatically saved and used in subsequent requests

## Collection Structure

### 1. Setup & Authentication
- Create initial data (Statuses, Roles, Team)
- User signup and login
- Automatically saves IDs and JWT token to environment variables

### 2. Employees
- CRUD operations for employees
- Advanced querying with filters, sorting, pagination
- Get employees by team or manager

### 3. Projects
- Create and manage projects
- Add/remove project members
- Query with filters and sorting

### 4. Tickets
- Create and manage tickets
- Update ticket status
- Advanced filtering (by status, project, employee, etc.)

### 5. Daily Updates
- Create daily standup updates
- Link tickets to updates
- View all updates

### 6. Reports & Dashboard
- Overall dashboard
- Employee workload
- Team progress
- Project health
- Daily standup reports
- Manager view

## Environment Variables

The collection automatically manages these variables:

- `jwt_token` - JWT authentication token
- `team_id` - Current team ID
- `role_admin_id`, `role_manager_id`, `role_user_id` - Role IDs
- `status_todo_id`, `status_inprogress_id`, `status_completed_id` - Status IDs
- `employee_id` - Current employee ID
- `manager_id` - Manager ID
- `project_id` - Current project ID
- `ticket_id` - Current ticket ID
- `daily_update_id` - Current daily update ID

## Advanced Query Examples

### Employees
```
GET /api/v1/employees?page=0&size=10&sort=name:asc&search=john&filter=teamId:1
```

### Tickets
```
GET /api/v1/tickets?page=0&size=20&sort=created_at:desc&filter=statusId:1,projectId:5&search=authentication
```

### Projects
```
GET /api/v1/projects?sort=projectName:asc&filter=teamId:2
```

## Filter Operators

- `eq` - Equals (default)
- `ne` - Not equals
- `gt` - Greater than
- `gte` - Greater than or equal
- `lt` - Less than
- `lte` - Less than or equal
- `like` - Contains (case-insensitive)
- `in` - In list (use `|` separator: `1|2|3`)
- `between` - Between two values (use `:` separator: `2024-01-01:2024-12-31`)

## Automated Testing

The collection includes automated scripts that:
- Save response IDs to environment variables
- Extract JWT tokens from login/signup responses
- Chain requests together (create → use ID in next request)

## Running the Complete Flow

1. Run all requests in "1. Setup & Authentication" folder in order
2. This will create all necessary data and authenticate
3. Then you can test any other endpoint - they'll use the saved tokens and IDs

## Troubleshooting

- **401 Unauthorized**: Make sure you've run the Login request and JWT token is saved
- **404 Not Found**: Check that IDs in environment variables are correct
- **400 Bad Request**: Verify request body matches the expected format
- **500 Server Error**: Check server logs and ensure database is running

## Notes

- All authenticated endpoints require JWT token (automatically added via Bearer token)
- The collection uses environment variables to chain requests
- Query parameters support pagination, sorting, filtering, and searching
- See `QUERY_SYSTEM_GUIDE.md` for detailed query syntax
