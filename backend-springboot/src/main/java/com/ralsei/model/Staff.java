package com.ralsei.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.Table;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "staff")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Staff extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "staffId")
    private int staffId;

    @Column(name = "accountId", unique = true)
    private Integer accountId;

    @Column(name = "ticketAgencyId")
    private Integer ticketAgencyId;

    @Column(name = "staffName", nullable = false)
    private String staffName;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "dob")
    private LocalDate dob;

    @Column(name = "cccd")
    private String cccd;

    @Column(name = "staffPosition", nullable = false)
    private String staffPosition;

    @Column(name = "hireDate", nullable = false)
    private LocalDate hireDate;

    @Column(name = "isActive", nullable = false)
    private boolean isActive;

    @JsonIgnore
    @OneToMany(mappedBy = "soldBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CargoTicket> cargoTickets;

    @JsonIgnore
    @OneToMany(mappedBy = "loadedBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CargoTicket> loadedCargoTickets;

    @JsonIgnore
    @OneToMany(mappedBy = "unloadedBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CargoTicket> unloadedCargoTickets;

    @JsonIgnore
    @OneToMany(mappedBy = "deliveredBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CargoTicket> deliveredCargoTickets;
}