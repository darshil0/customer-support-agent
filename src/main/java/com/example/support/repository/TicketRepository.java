package com.example.support.repository;

import com.example.support.entity.Ticket;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, String> {
  List<Ticket> findByCustomerId(String customerId);

  List<Ticket> findByCustomerIdAndStatus(String customerId, String status);
}
