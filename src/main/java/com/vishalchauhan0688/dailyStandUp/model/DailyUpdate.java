package com.vishalchauhan0688.dailyStandUp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.List;

/**
 * This class represent daily_update table
 *
 * TODO: enforce only one post per user per day
 */
@Entity
@Getter
@Setter
public class DailyUpdate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Lob
    @Column(nullable = false)
    @NotNull
    private String generalDescription;
    //emp
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id" ,nullable = false)
    Employee employee;
    //ticket mention
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    List<TicketMention> ticketMentions;
    @CreationTimestamp
    private Instant createdAt;
    @UpdateTimestamp
    private Instant updatedAt;
}
