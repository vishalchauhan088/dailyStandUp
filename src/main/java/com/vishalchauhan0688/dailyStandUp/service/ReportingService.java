package com.vishalchauhan0688.dailyStandUp.service;

import com.vishalchauhan0688.dailyStandUp.dto.EmployeeResponseDto;
import com.vishalchauhan0688.dailyStandUp.dto.QueryParams;
import com.vishalchauhan0688.dailyStandUp.exception.BadRequestException;
import com.vishalchauhan0688.dailyStandUp.model.*;
import com.vishalchauhan0688.dailyStandUp.repository.DailyUpdatePostRepository;
import com.vishalchauhan0688.dailyStandUp.repository.EmployeeTeamRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportingService {

        private final EmployeeService employeeService;
        private final TicketService ticketService;
        private final ProjectService projectService;
        private final TeamService teamService;
        private final DailyUpdatePostRepository dailyUpdatePostRepository;
        private final TicketDependencyService dependencyService;
        private final EmployeeTeamRoleRepository employeeTeamRoleRepository;
        private final AuthorizationService authorizationService;

        /* ===================== EMPLOYEE ===================== */

        public Map<String, Object> getEmployeeWorkload(Long employeeId) {
                Employee employee = employeeService.findByIdEntity(employeeId)
                                .orElseThrow(() -> new RuntimeException("Employee not found"));

                List<Ticket> tickets = fetchTickets("owner.id:eq:" + employeeId);

                return Map.of(
                                "employeeId", employeeId,
                                "employeeName", employee.getName(),
                                "totalTickets", tickets.size(),
                                "statusDistribution", statusDistribution(tickets),
                                "tickets", tickets);
        }

        /* ===================== TEAM ===================== */

        public Map<String, Object> getTeamProgress(Long teamId) {
                Team team = teamService.findById(teamId);
                List<Project> projects = fetchProjects("team.id:eq:" + teamId);

                // Get all team members through EmployeeTeamRole
                List<EmployeeTeamRole> teamRoles = employeeTeamRoleRepository.findByTeamId(teamId);
                List<Long> employeeIds = teamRoles.stream()
                                .map(etr -> etr.getEmployee().getId())
                                .collect(Collectors.toList());

                List<Ticket> teamTickets = employeeIds.stream()
                                .flatMap(empId -> fetchTickets("owner.id:eq:" + empId).stream())
                                .collect(Collectors.toList());

                List<Map<String, Object>> projectProgress = projects.stream()
                                .map(this::projectProgress)
                                .collect(Collectors.toList());

                return Map.of(
                                "teamId", teamId,
                                "teamName", team.getTeamName(),
                                "memberCount", teamRoles.size(),
                                "totalTickets", teamTickets.size(),
                                "statusDistribution", statusDistribution(teamTickets),
                                "projectProgress", projectProgress);
        }

        /* ===================== PROJECT ===================== */

        public Map<String, Object> getProjectHealth(Long projectId) {
                Project project = projectService.findById(projectId);
                List<Ticket> tickets = fetchTickets("project.id:eq:" + projectId);

                LocalDate today = LocalDate.now();

                List<Map<String, Object>> blocked = tickets.stream()
                                .filter(t -> !dependencyService
                                                .findUnresolvedDependenciesByTicketId(t.getId()).isEmpty())
                                .map(this::blockedTicketView)
                                .collect(Collectors.toList());

                List<Map<String, Object>> late = tickets.stream()
                                .filter(t -> t.getEndDate() != null
                                                && t.getEndDate().isBefore(today)
                                                && !t.getStatus().getStatus().equalsIgnoreCase("DONE"))
                                .map(t -> lateTicketView(t, today))
                                .collect(Collectors.toList());

                return Map.of(
                                "projectId", projectId,
                                "projectName", project.getProjectName(),
                                "totalTickets", tickets.size(),
                                "blockedTickets", blocked,
                                "blockedCount", blocked.size(),
                                "lateTickets", late,
                                "lateCount", late.size(),
                                "statusDistribution", statusDistribution(tickets),
                                "healthScore", calculateHealthScore(
                                                tickets.size(), blocked.size(), late.size()));
        }

        /* ===================== DAILY STANDUP ===================== */

        /**
         * Get daily standup report for a specific date.
         * Optionally filter by team.
         * 
         * @param date   The date to get reports for
         * @param teamId Optional team ID to filter updates. If null, uses the logged-in
         *               user's teams
         */
        public Map<String, Object> getDailyStandupReport(LocalDate date, Long teamId) {
                Employee loggedInEmployee = employeeService.getMe();
                Long employeeId = loggedInEmployee.getId();
                boolean isGlobalAdmin = authorizationService.isGlobalAdmin(employeeId);

                List<DailyUpdatePost> updates;

                if (teamId != null) {
                        // Specific team requested - verify access
                        if (!isGlobalAdmin && !authorizationService.isTeamMember(employeeId, teamId)) {
                                throw new BadRequestException("You don't have access to team " + teamId);
                        }
                        updates = dailyUpdatePostRepository.findByTeamIdAndDate(teamId, date);
                } else if (!isGlobalAdmin) {
                        // Non-admin: get updates from user's teams only
                        List<Long> userTeamIds = employeeTeamRoleRepository.findByEmployeeId(employeeId)
                                        .stream()
                                        .map(etr -> etr.getTeam().getId())
                                        .toList();

                        if (userTeamIds.isEmpty()) {
                                updates = Collections.emptyList();
                        } else {
                                updates = userTeamIds.stream()
                                                .flatMap(tid -> dailyUpdatePostRepository.findByTeamIdAndDate(tid, date)
                                                                .stream())
                                                .collect(Collectors.toList());
                        }
                } else {
                        // Global admin: get all updates
                        updates = dailyUpdatePostRepository.findByDate(date);
                }

                List<Map<String, Object>> employeeUpdates = updates.stream()
                                .map(this::dailyUpdateView)
                                .collect(Collectors.toList());

                Map<String, Object> result = new LinkedHashMap<>();
                result.put("date", date);
                result.put("totalUpdates", updates.size());
                result.put("employeeUpdates", employeeUpdates);

                if (teamId != null) {
                        Team team = teamService.findById(teamId);
                        result.put("teamId", teamId);
                        result.put("teamName", team.getTeamName());
                }

                return result;
        }

        /* ===================== OVERALL ===================== */

        public Map<String, Object> getOverallDashboard() {
                List<Team> teams = teamService.findAll();
                List<Project> projects = projectService.findAll();
                List<Ticket> tickets = ticketService.findAll().stream()
                                .filter(t -> t.getDeletedAt() == null)
                                .collect(Collectors.toList());

                return Map.of(
                                "totalTeams", teams.size(),
                                "totalProjects", projects.size(),
                                "totalTickets", tickets.size(),
                                "statusDistribution", statusDistribution(tickets),
                                "teams", teams.stream()
                                                .map(t -> Map.of("id", t.getId(), "name", t.getTeamName()))
                                                .collect(Collectors.toList()),
                                "projects", projects.stream()
                                                .map(p -> Map.of("id", p.getId(), "name", p.getProjectName()))
                                                .collect(Collectors.toList()));
        }

        /* ===================== HELPERS ===================== */

        private QueryParams filter(String filter) {
                return QueryParams.builder().filter(filter).page(0).size(1000).build();
        }

        private List<Ticket> fetchTickets(String filter) {
                return ticketService.findAll(filter(filter)).getContent();
        }

        private List<Project> fetchProjects(String filter) {
                return projectService.findAll(filter(filter)).getContent();
        }

        private Map<String, Long> statusDistribution(List<Ticket> tickets) {
                return tickets.stream()
                                .collect(Collectors.groupingBy(
                                                t -> t.getStatus().getStatus(),
                                                Collectors.counting()));
        }

        private Map<String, Object> projectProgress(Project project) {
                List<Ticket> tickets = fetchTickets("project.id:eq:" + project.getId());
                return Map.of(
                                "projectId", project.getId(),
                                "projectName", project.getProjectName(),
                                "totalTickets", tickets.size(),
                                "statusDistribution", statusDistribution(tickets));
        }

        private Map<String, Object> blockedTicketView(Ticket t) {
                return Map.of(
                                "ticketId", t.getId(),
                                "jiraId", t.getJiraId(),
                                "title", t.getTitle(),
                                "blockingDependencies",
                                dependencyService.findUnresolvedDependenciesByTicketId(t.getId()).size());
        }

        private Map<String, Object> lateTicketView(Ticket t, LocalDate today) {
                return Map.of(
                                "ticketId", t.getId(),
                                "jiraId", t.getJiraId(),
                                "title", t.getTitle(),
                                "endDate", t.getEndDate(),
                                "daysOverdue", ChronoUnit.DAYS.between(t.getEndDate(), today));
        }

        private Map<String, Object> dailyUpdateView(DailyUpdatePost u) {
                return Map.of(
                                "employeeId", u.getEmployee().getId(),
                                "employeeName", u.getEmployee().getName(),
                                "teamId", u.getTeam().getId(),
                                "teamName", u.getTeam().getTeamName(),
                                "date", u.getDate(),
                                "generalNotes", u.getGeneralNotes() != null ? u.getGeneralNotes() : "",
                                "ticketMentions", u.getTicketMentions().stream()
                                                .map(tm -> Map.of(
                                                                "ticketId", tm.getTicket().getId(),
                                                                "jiraId", tm.getTicket().getJiraId(),
                                                                "title", tm.getTicket().getTitle(),
                                                                "description", tm.getDescription()))
                                                .collect(Collectors.toList()),
                                "createdAt", u.getCreatedAt());
        }

        private double calculateHealthScore(int total, int blocked, int late) {
                if (total == 0)
                        return 100.0;
                return Math.max(0,
                                100 - ((blocked * 10.0 + late * 5.0) / total) * 100);
        }
}
