package com.ralsei.repository;

import com.ralsei.model.CargoTicketDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CargoTicketDetailRepository extends JpaRepository<CargoTicketDetail, Integer> {
    List<CargoTicketDetail> findByCargoTicketId(int cargoTicketId);
}
