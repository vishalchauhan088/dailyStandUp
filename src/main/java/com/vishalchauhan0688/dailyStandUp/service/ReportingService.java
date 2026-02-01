package com.vishalchauhan0688.dailyStandUp.service;

import com.vishalchauhan0688.dailyStandUp.dto.EmployeeResponseDto;
import com.vishalchauhan0688.dailyStandUp.dto.QueryParams;
import com.vishalchauhan0688.dailyStandUp.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
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
    private final DailyUpdateService dailyUpdateService;
    private final TicketDependencyService dependencyService;

    /* ===================== EMPLOYEE ===================== */

    public Map<String, Object> getEmployeeWorkload(Long employeeId) {

        Employee employee = employeeService.findByIdEntity(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        List<Ticket> tickets = fetchTickets("createdBy.id:eq:" + employeeId);

        return Map.of(
                "employeeId", employeeId,
                "employeeName", employee.getName(),
                "totalTickets", tickets.size(),
                "statusDistribution", statusDistribution(tickets),
                "tickets", tickets
        );
    }

    /* ===================== TEAM ===================== */

    public Map<String, Object> getTeamProgress(Long teamId) {

        Team team = teamService.findById(teamId);
        List<Project> projects = fetchProjects("team.id:eq:" + teamId);

        List<Ticket> teamTickets = team.getEmployees().stream()
                .flatMap(emp -> fetchTickets("createdBy.id:eq:" + emp.getId()).stream())
                .collect(Collectors.toList());

        List<Map<String, Object>> projectProgress = projects.stream()
                .map(p -> projectProgress(p))
                .collect(Collectors.toList());

        return Map.of(
                "teamId", teamId,
                "teamName", team.getTeamName(),
                "memberCount", team.getEmployees().size(),
                "totalTickets", teamTickets.size(),
                "statusDistribution", statusDistribution(teamTickets),
                "projectProgress", projectProgress
        );
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
                        && !t.getStatus().getStatus().equalsIgnoreCase("completed"))
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
                        tickets.size(), blocked.size(), late.size()
                )
        );
    }

    /* ===================== DAILY STANDUP ===================== */

    public Map<String, Object> getDailyStandupReport(LocalDate date) {

        ZoneId zone = ZoneId.systemDefault(); // or ZoneOffset.UTC if you standardize on UTC

        List<DailyUpdate> updates = dailyUpdateService.findAll().stream()
                .filter(u ->
                        u.getCreatedAt() != null &&
                                u.getCreatedAt()
                                        .atZone(zone)
                                        .toLocalDate()
                                        .equals(date)
                )
                .toList();

        List<Map<String, Object>> employeeUpdates = updates.stream()
                .map(this::dailyUpdateView)
                .toList();

        return Map.of(
                "date", date,
                "totalUpdates", updates.size(),
                "employeeUpdates", employeeUpdates
        );
    }


    /* ===================== MANAGER ===================== */

    public Map<String, Object> getManagerView(Long managerId) {

        Employee manager = employeeService.findByIdEntity(managerId)
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        List<EmployeeResponseDto> subs = employeeService
                .findAll(filter("manager.id:eq:" + managerId))
                .getContent();

        List<Map<String, Object>> subordinateWork = subs.stream()
                .map(sub -> employeeWorkView(sub))
                .collect(Collectors.toList());

        return Map.of(
                "managerId", managerId,
                "managerName", manager.getName(),
                "subordinateCount", subs.size(),
                "subordinateWork", subordinateWork
        );
    }

    /* ===================== OVERALL ===================== */

    public Map<String, Object> getOverallDashboard() {

        List<Team> teams = teamService.findAll();
        List<Project> projects = projectService.findAll();
        List<Ticket> tickets = ticketService.findAll();

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
                        .collect(Collectors.toList())
        );
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
                        Collectors.counting()
                ));
    }

    private Map<String, Object> projectProgress(Project project) {
        List<Ticket> tickets = fetchTickets("project.id:eq:" + project.getId());
        return Map.of(
                "projectId", project.getId(),
                "projectName", project.getProjectName(),
                "totalTickets", tickets.size(),
                "statusDistribution", statusDistribution(tickets)
        );
    }

    private Map<String, Object> blockedTicketView(Ticket t) {
        return Map.of(
                "ticketId", t.getId(),
                "jiraId", t.getExternalId(),
                "title", t.getTitle(),
                "blockingDependencies",
                dependencyService.findUnresolvedDependenciesByTicketId(t.getId()).size()
        );
    }

    private Map<String, Object> lateTicketView(Ticket t, LocalDate today) {
        return Map.of(
                "ticketId", t.getId(),
                "jiraId", t.getExternalId(),
                "title", t.getTitle(),
                "endDate", t.getEndDate(),
                "daysOverdue", ChronoUnit.DAYS.between(t.getEndDate(), today)
        );
    }

    private Map<String, Object> dailyUpdateView(DailyUpdate u) {
        return Map.of(
                "employeeId", u.getEmployee().getId(),
                "employeeName", u.getEmployee().getName(),
                "generalDescription", u.getGeneralDescription(),
                "ticketMentions", u.getTicketMentions().stream()
                        .map(tm -> Map.of(
                                "ticketId", tm.getTicket().getId(),
                                "jiraId", tm.getTicket().getExternalId(),
                                "title", tm.getTicket().getTitle(),
                                "description", tm.getDescription()
                        ))
                        .collect(Collectors.toList()),
                "createdAt", u.getCreatedAt()
        );
    }

    private Map<String, Object> employeeWorkView(EmployeeResponseDto emp) {
        List<Ticket> tickets = fetchTickets("createdBy.id:eq:" + emp.getId());
        return Map.of(
                "employeeId", emp.getId(),
                "employeeName", emp.getName(),
                "totalTickets", tickets.size(),
                "statusDistribution", statusDistribution(tickets),
                "tickets", tickets
        );
    }

    private double calculateHealthScore(int total, int blocked, int late) {
        if (total == 0) return 100.0;
        return Math.max(0,
                100 - ((blocked * 10.0 + late * 5.0) / total) * 100
        );
    }
}
