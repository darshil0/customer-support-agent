package com.example.support.controller;

import static org.mockito.Mockito.when;

import com.example.support.CustomerSupportAgent;
import com.example.support.entity.Customer;
import com.example.support.logging.CustomLogger;
import com.example.support.repository.CustomerRepository;
import com.example.support.repository.TicketRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.graphql.test.tester.GraphQlTester;

@GraphQlTest(SupportGraphQLController.class)
public class SupportGraphQLControllerTest {

  @Autowired private GraphQlTester graphQlTester;

  @MockBean private CustomerSupportAgent agent;
  @MockBean private CustomerRepository customerRepository;
  @MockBean private TicketRepository ticketRepository;
  @MockBean private CustomLogger customLogger;

  @Test
  public void testCustomerQuery() {
    Customer mockCustomer = new Customer();
    mockCustomer.setCustomerId("CUST001");
    mockCustomer.setName("John Doe");

    when(customerRepository.findById("CUST001")).thenReturn(Optional.of(mockCustomer));

    String query = "{ customer(customerId: \"CUST001\") { name } }";

    graphQlTester
        .document(query)
        .execute()
        .errors()
        .verify()
        .path("customer.name")
        .entity(String.class)
        .isEqualTo("John Doe");
  }
}
