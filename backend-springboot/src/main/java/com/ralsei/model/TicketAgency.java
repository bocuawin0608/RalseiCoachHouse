package com.ralsei.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ticket_agency")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
/**
 * TicketAgency entity.
 */

/**
 * Provides the ticket agency component for the application.
 */
public class TicketAgency extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticketAgencyId")
    private int ticketAgencyId;

    @Column(name = "stopPointId", nullable = false)
    private int stopPointId;

    @Column(name = "ticketAgencyName", nullable = false)
    private String ticketAgencyName;

    @Column(name = "isActive", nullable = false)
    private boolean isActive;

}