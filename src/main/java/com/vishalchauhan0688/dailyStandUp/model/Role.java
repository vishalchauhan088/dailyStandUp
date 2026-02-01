package com.vishalchauhan0688.dailyStandUp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_name", nullable = false, unique = true, length = 50)
    @NotBlank(message = "Role name is required")
    private String roleName;

    /**
     * Role type determines where this role applies:
     * - SYSTEM: Global permissions (ADMIN) - applies everywhere
     * - TEAM: Team-specific permissions (OWNER, MANAGER, MEMBER) - per-team basis
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role_type", nullable = false, length = 20)
    @Builder.Default
    private RoleType roleType = RoleType.TEAM;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public enum RoleType {
        SYSTEM, // Global roles like ADMIN - apply across all teams
        TEAM // Team-specific roles like OWNER, MANAGER, MEMBER
    }

    /**
     * Check if this is a system-level role
     */
    public boolean isSystemRole() {
        return this.roleType == RoleType.SYSTEM;
    }

    /**
     * Check if this is a team-specific role
     */
    public boolean isTeamRole() {
        return this.roleType == RoleType.TEAM;
    }
}
