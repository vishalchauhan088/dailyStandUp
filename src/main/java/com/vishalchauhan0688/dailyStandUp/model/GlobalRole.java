package com.vishalchauhan0688.dailyStandUp.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * Global/System-level roles that apply across all teams.
 * Examples: ADMIN, SUPER_ADMIN
 */
@Entity
@Table(name = "global_roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GlobalRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    @Enumerated(EnumType.STRING)
    private GlobalRoleName name;

    @Column(length = 255)
    private String description;

    /**
     * Employees who have this global role.
     * Note: We use the join entity for explicit ownership tracking.
     */
    @OneToMany(mappedBy = "globalRole", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<EmployeeGlobalRole> employeeRoles = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public enum GlobalRoleName {
        ADMIN, // Full system access
        SUPER_ADMIN // Can manage other admins
    }

    /**
     * Check if this is the ADMIN role
     */
    public boolean isAdmin() {
        return this.name == GlobalRoleName.ADMIN;
    }

    /**
     * Check if this is the SUPER_ADMIN role
     */
    public boolean isSuperAdmin() {
        return this.name == GlobalRoleName.SUPER_ADMIN;
    }
}
