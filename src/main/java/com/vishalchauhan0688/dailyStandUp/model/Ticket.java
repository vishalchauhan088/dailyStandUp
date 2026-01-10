package com.vishalchauhan0688.dailyStandUp.model;

import com.vishalchauhan0688.dailyStandUp.Enum.TicketStatus;
import jakarta.persistence.*;
import jdk.jfr.Timespan;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Getter
@Setter
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    String externalId;
    @Column(nullable = false)
    String title;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id")
    Employee createdBy;
    @Enumerated(EnumType.STRING)
    TicketStatus status;
    @CreationTimestamp
    Instant createdAt;
    @UpdateTimestamp
    Instant updatedAt;

    @PrePersist
    public void prePersist() {
        if (status == null) {
            status = TicketStatus.TODO;
        }
    }


}
