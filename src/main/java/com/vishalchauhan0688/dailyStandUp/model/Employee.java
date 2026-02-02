package com.vishalchauhan0688.dailyStandUp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "employees", indexes = {
                @Index(name = "index_employee_username", columnList = "username")
})
@Getter
@Setter
@ToString(exclude = { "password" })
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "username", nullable = false, unique = true, length = 255)
        @NotBlank(message = "Username is required")
        private String username;

        @Column(name = "name", nullable = false, length = 255)
        @NotBlank(message = "Name is required")
        private String name;

        @Column(nullable = false, unique = true, length = 255)
        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        private String email;

        @Column(nullable = false)
        @NotBlank(message = "Password is required")
        private String password;

        /**
         * Global/System roles (ADMIN, SUPER_ADMIN) - apply across all teams
         */
        @OneToMany(mappedBy = "employee", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
        @Builder.Default
        private List<EmployeeGlobalRole> globalRoles = new ArrayList<>();

        /**
         * Team-specific roles (OWNER, MANAGER, MEMBER) - per-team basis
         */
        @OneToMany(mappedBy = "employee", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
        @Builder.Default
        private List<EmployeeTeamRole> teamRoles = new ArrayList<>();

        @OneToMany(mappedBy = "employee", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
        @Builder.Default
        private List<TeamJoinRequest> joinRequests = new ArrayList<>();

        @OneToMany(mappedBy = "employee", fetch = FetchType.LAZY)
        @Builder.Default
        private List<DailyUpdatePost> dailyUpdatePosts = new ArrayList<>();

        @ManyToMany(mappedBy = "employees", fetch = FetchType.LAZY)
        @Builder.Default
        private Set<Project> projects = new HashSet<>();

        @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY)
        @Builder.Default
        private List<Ticket> ownedTickets = new ArrayList<>();

        @UpdateTimestamp
        @Column(name = "updated_at", nullable = false)
        private Instant updatedAt;

        @CreationTimestamp
        @Column(name = "created_at", updatable = false, nullable = false)
        private Instant createdAt;

        /**
         * Check if employee has global ADMIN role
         */
        public boolean isGlobalAdmin() {
                return globalRoles.stream()
                                .anyMatch(gr -> gr.getGlobalRole() != null &&
                                                gr.getGlobalRole().isAdmin());
        }

        /**
         * Check if employee has global SUPER_ADMIN role
         */
        public boolean isSuperAdmin() {
                return globalRoles.stream()
                                .anyMatch(gr -> gr.getGlobalRole() != null &&
                                                gr.getGlobalRole().isSuperAdmin());
        }

        /**
         * Get all global role names for this employee
         */
        public Set<String> getGlobalRoleNames() {
                return globalRoles.stream()
                                .map(EmployeeGlobalRole::getRoleName)
                                .collect(java.util.stream.Collectors.toSet());
        }

        /**
         * Get all team role names for this employee
         */
        public Set<String> getTeamRoleNames() {
                return teamRoles.stream()
                                .map(etr -> etr.getTeamRole() != null ? etr.getTeamRole().getName() : null)
                                .filter(name -> name != null)
                                .collect(java.util.stream.Collectors.toSet());
        }

        /**
         * Get all role names (both global and team-specific)
         */
        public Set<String> getAllRoleNames() {
                Set<String> allRoles = new HashSet<>(getGlobalRoleNames());
                allRoles.addAll(getTeamRoleNames());
                return allRoles;
        }
}
