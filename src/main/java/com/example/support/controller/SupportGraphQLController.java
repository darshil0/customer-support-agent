package com.example.support.controller;

import com.example.support.CustomerSupportAgent;
import com.example.support.entity.Customer;
import com.example.support.entity.Ticket;
import com.example.support.logging.CustomLogger;
import com.example.support.repository.CustomerRepository;
import com.example.support.repository.TicketRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class SupportGraphQLController {

  @Autowired private CustomerSupportAgent agent;

  @Autowired private CustomerRepository customerRepository;

  @Autowired private TicketRepository ticketRepository;

  @Autowired private CustomLogger logger;

  @QueryMapping
  public Customer customer(@Argument String customerId) {
    logger.info(SupportGraphQLController.class, "GraphQL: Fetching customer " + customerId);
    return customerRepository.findById(customerId).orElse(null);
  }

  @QueryMapping
  public List<Ticket> tickets(@Argument String customerId, @Argument String status) {
    if (status == null || status.equalsIgnoreCase("all")) {
      return ticketRepository.findByCustomerId(customerId);
    }
    return ticketRepository.findByCustomerIdAndStatus(customerId, status.toLowerCase());
  }

  @QueryMapping
  public Map<String, Object> analytics() {
    logger.info(SupportGraphQLController.class, "GraphQL: Fetching analytics");
    List<Ticket> allTickets = ticketRepository.findAll();
    Map<String, Long> statusDistribution =
        allTickets.stream()
            .collect(Collectors.groupingBy(Ticket::getStatus, Collectors.counting()));

    List<Customer> allCustomers = customerRepository.findAll();
    Map<String, Long> tierBreakdown =
        allCustomers.stream()
            .collect(Collectors.groupingBy(Customer::getTier, Collectors.counting()));

    double totalBalance = allCustomers.stream().mapToDouble(Customer::getBalance).sum();

    return Map.of(
        "ticketStatusDistribution",
        statusDistribution.entrySet().stream()
            .map(e -> Map.of("status", e.getKey(), "count", e.getValue()))
            .collect(Collectors.toList()),
        "customerTierBreakdown",
        tierBreakdown.entrySet().stream()
            .map(e -> Map.of("tier", e.getKey(), "count", e.getValue()))
            .collect(Collectors.toList()),
        "totalRevenue",
        totalBalance // Simulated revenue
        );
  }

  @MutationMapping
  public Map<String, Object> processPayment(@Argument String customerId, @Argument Double amount) {
    Map<String, Object> result = agent.processPayment(customerId, amount, null);
    boolean success = (boolean) result.get("success");
    if (success) {
      Map<String, Object> data = (Map<String, Object>) result.get("data");
      return Map.of(
          "success", true,
          "transactionId", data.get("transactionId"),
          "newBalance", data.get("newBalance"),
          "message", result.get("message"));
    } else {
      return Map.of("success", false, "message", result.get("error"));
    }
  }

  @MutationMapping
  public Ticket createTicket(
      @Argument String customerId,
      @Argument String subject,
      @Argument String description,
      @Argument String priority) {
    Map<String, Object> result =
        agent.createTicket(customerId, subject, description, priority, null);
    if ((boolean) result.get("success")) {
      // Since createTicket returns a Map, we might need to fetch it from repo or reconstruct
      // For simplicity, let's assume it's saved and we return the map structure which GraphQL can
      // map to Ticket
      String ticketId = (String) ((Map<String, Object>) result.get("data")).get("ticketId");
      return ticketRepository.findById(ticketId).orElse(null);
    }
    return null;
  }

  @MutationMapping
  public Map<String, Object> updateAccountSettings(
      @Argument String customerId, @Argument String email, @Argument String tier) {
    Map<String, Object> result = agent.updateAccountSettings(customerId, email, tier, null);
    if ((boolean) result.get("success")) {
      return Map.of("success", true, "message", result.get("message"));
    } else {
      return Map.of("success", false, "message", result.get("error"));
    }
  }
}
