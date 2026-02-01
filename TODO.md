# Daily Update Improvements - Implementation Plan

## Objective
Add team filtering to daily updates and improve the overall functionality.

## Tasks

### 1. DailyUpdatePostRepository - Add EntityGraph to findByDate ✅
- [x] Add @EntityGraph to findByDate method for eager loading

### 2. DailyUpdateResponseDto - Include Team Info ✅
- [x] Add teamId and teamName fields to DTO (already exists)
- [x] Update fromEntity method to include team data (already exists)

### 3. DailyUpdateService - Team-Based Filtering & Authorization ✅
- [x] Add method to get user's team IDs
- [x] Add teamId parameter support in findAll method
- [x] Add authorization: only show updates from user's teams (or all for admins)
- [x] Expand search to include employee name, team name, ticket titles

### 4. DailyUpdateController - Add teamId Query Parameter ✅
- [x] Add teamId request parameter
- [x] Pass teamId to service layer

### 5. ReportingService - Add Team Filtering to Standup Report ✅
- [x] Add teamId parameter to getDailyStandupReport method
- [x] Filter updates by team when teamId is provided

### 6. ReportingController - Add teamId Query Parameter ✅
- [x] Add teamId request parameter to standup endpoint

## Implementation Order
1. Repository updates (EntityGraph) ✅
2. DTO updates (team info) ✅
3. Service layer (filtering + authorization + expanded search) ✅
4. Controller updates (new parameters) ✅
5. Reporting updates ✅

## Testing Notes
- Test team filtering: GET /api/v1/dailyupdates?teamId=1
- Test admin view: Admin should see all teams' updates
- Test team member view: Should only see their teams' updates
- Test expanded search: Search by employee name, team name, ticket titles
- Test reporting: GET /api/v1/reports/standup?date=2024-01-15&teamId=1

