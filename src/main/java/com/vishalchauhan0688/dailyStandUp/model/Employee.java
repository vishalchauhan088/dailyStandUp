package com.vishalchauhan0688.dailyStandUp.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;


@Entity
@Table(
        name="employees"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column( nullable = false)
    private String firstName;
    private String lastName;
    @Column(nullable = false, unique = true)
    private String email;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name="manager_id",
            nullable = true
    )
    private Employee manager;
    private String password;
    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updated_at;
    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private Instant created_at;


}
