package com.example.support.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.support.entity.Customer;
import com.example.support.entity.Ticket;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
public class TicketRepositoryTest {

  @Autowired private TicketRepository repository;
  @Autowired private CustomerRepository customerRepository;

  @Test
  public void testFindByCustomerId() {
    Customer customer =
        new Customer(
            "CUST_T", "Test", "test@test.com", "Basic", 0.0, LocalDateTime.now(), "active");
    customerRepository.save(customer);

    Ticket ticket =
        new Ticket(
            "TICK001", "CUST_T", "Test Subject", "Test Desc", "low", "open", LocalDateTime.now());
    repository.save(ticket);

    List<Ticket> tickets = repository.findByCustomerId("CUST_T");
    assertThat(tickets).hasSize(1);
    assertThat(tickets.get(0).getSubject()).isEqualTo("Test Subject");
  }
}
