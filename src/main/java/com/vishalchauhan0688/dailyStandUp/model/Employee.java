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
@Table(
        name = "employees",
        indexes = {
                @Index(name = "index_employee_username", columnList = "username")
        }
)
@Getter
@Setter
@ToString(exclude = {"password", "manager", "subordinates"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 255)
    @NotBlank(message = "Username is required")
    private String userName;

    @Column(name = "name", nullable = false, length = 255)
    @NotBlank(message = "Name is required")
    private String name;

    @Column(nullable = false, unique = true, length = 255)
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", nullable = true)
    private Employee manager;

    @OneToMany(mappedBy = "manager", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Employee> subordinates = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(nullable = false)
    @NotBlank(message = "Password is required")
    private String password;

    @OneToMany(mappedBy = "employee", fetch = FetchType.LAZY)
    @Builder.Default
    private List<DailyUpdate> dailyUpdates = new ArrayList<>();

    @ManyToMany(mappedBy = "employees", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Project> projects = new HashSet<>();

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updated_at;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant created_at;
}
