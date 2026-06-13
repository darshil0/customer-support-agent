package com.example.support.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.support.entity.Customer;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
public class CustomerRepositoryTest {

  @Autowired private CustomerRepository repository;

  @Test
  public void testSaveAndFind() {
    Customer customer =
        new Customer(
            "TEST001",
            "Test User",
            "test@example.com",
            "Basic",
            100.0,
            LocalDateTime.now(),
            "active");
    repository.save(customer);

    Optional<Customer> found = repository.findById("TEST001");
    assertThat(found).isPresent();
    assertThat(found.get().getName()).isEqualTo("Test User");
  }
}
