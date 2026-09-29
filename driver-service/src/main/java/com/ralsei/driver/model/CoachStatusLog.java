package com.ralsei.driver.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "coach_status_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoachStatusLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coachStatusLogId")
    private int coachStatusLogId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coachId", nullable = false)
    private Coach coach;

    @Enumerated(EnumType.STRING)
    @Column(name = "fromStatus")
    private CoachStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "toStatus", nullable = false)
    private CoachStatus toStatus;

    @Column(name = "reason")
    private String reason;

    @Column(name = "expectedEndAt")
    private LocalDateTime expectedEndAt;

    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
