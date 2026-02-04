package com.vishalchauhan0688.dailyStandUp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Join entity to track which employees have which global/system roles.
 * This separates global roles from team-specific roles.
 */
@Entity
@Table(name = "employee_global_roles", uniqueConstraints = {
        @UniqueConstraint(name = "uniq_employee_global_role", columnNames = { "employee_id", "global_role_id" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeGlobalRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonIgnore // Prevent circular reference: Employee -> EmployeeGlobalRole -> Employee
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "global_role_id", nullable = false)
    private GlobalRole globalRole;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Get the name of the global role
     */
    public String getRoleName() {
        return globalRole != null ? globalRole.getName().name() : null;
    }
}
