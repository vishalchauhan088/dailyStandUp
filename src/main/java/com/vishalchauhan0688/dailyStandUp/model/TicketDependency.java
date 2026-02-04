package com.vishalchauhan0688.dailyStandUp.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "ticket_dependency", indexes = {
        @Index(name = "idx_ticket_dependency", columnList = "ticket_id,depends_on_ticket_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketDependency {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "depends_on_ticket_id", nullable = false)
    private Ticket dependsOnTicket;

    @Column(nullable = false)
    @Builder.Default
    private Boolean resolved = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    public void validateNotSelfDependent() {
        if (ticket != null && dependsOnTicket != null && ticket.getId() != null && 
            ticket.getId().equals(dependsOnTicket.getId())) {
            throw new IllegalArgumentException("A ticket cannot depend on itself");
        }
    }
}
