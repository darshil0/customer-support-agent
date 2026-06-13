package com.example.support;

import com.example.support.entity.Customer;
import com.example.support.entity.Ticket;
import com.example.support.logging.CustomLogger;
import com.example.support.repository.CustomerRepository;
import com.example.support.repository.TicketRepository;
import com.example.support.service.NotificationService;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Core business logic for customer support operations. Provides 7 tools for multi-agent system. */
@Component
public class CustomerSupportAgent {

  @Autowired private CustomerRepository customerRepository;

  @Autowired private TicketRepository ticketRepository;

  @Autowired(required = false)
  private NotificationService notificationService;

  @Autowired private CustomLogger logger;

  /**
   * Tool 1: Get customer account details.
   *
   * @param customerId the customer ID
   * @param context tool context for caching
   * @return customer account data
   */
  public Map<String, Object> getCustomerAccount(String customerId, Map<String, Object> context) {
    Map<String, Object> result = new HashMap<>();

    // Validation
    if (!ValidationUtils.isValidCustomerId(customerId)) {
      result.put("success", false);
      result.put("error", "Invalid customer ID format");
      return result;
    }

    // Check cache
    String cacheKey = "customer_" + customerId;
    if (context != null && context.containsKey(cacheKey)) {
      result.put("success", true);
      result.put("data", context.get(cacheKey));
      result.put("cached", true);
      return result;
    }

    logger.info(CustomerSupportAgent.class, "Fetching customer account: " + customerId);
    // Retrieve customer
    Optional<Customer> customerOpt = customerRepository.findById(customerId);
    if (customerOpt.isEmpty()) {
      result.put("success", false);
      result.put("error", "Customer not found");
      return result;
    }

    Customer customer = customerOpt.get();
    Map<String, Object> customerMap = customerToMap(customer);

    // Cache and return
    if (context != null) {
      context.put(cacheKey, new HashMap<>(customerMap));
      result.put("cached", false);
    }

    result.put("success", true);
    result.put("data", customerMap);
    return result;
  }

  /**
   * Tool 2: Process payment.
   *
   * @param customerId the customer ID
   * @param amount the payment amount
   * @param context tool context
   * @return payment result
   */
  public Map<String, Object> processPayment(
      String customerId, Double amount, Map<String, Object> context) {
    Map<String, Object> result = new HashMap<>();

    // Validation
    if (!ValidationUtils.isValidCustomerId(customerId)) {
      result.put("success", false);
      result.put("error", "Invalid customer ID");
      return result;
    }

    if (amount == null) {
      result.put("success", false);
      result.put("error", "Amount is required");
      return result;
    }

    double paymentAmount = ValidationUtils.roundAmount(amount);

    if (!ValidationUtils.isValidAmount(paymentAmount)) {
      result.put("success", false);
      result.put("error", "Amount must be between 0 and 100000");
      return result;
    }

    // Get customer
    Optional<Customer> customerOpt = customerRepository.findById(customerId);
    if (customerOpt.isEmpty()) {
      result.put("success", false);
      result.put("error", "Customer not found");
      return result;
    }

    Customer customer = customerOpt.get();

    // Process payment
    double currentBalance = customer.getBalance();
    double newBalance = ValidationUtils.roundAmount(currentBalance + paymentAmount);
    customer.setBalance(newBalance);
    customerRepository.save(customer);

    logger.info(
        CustomerSupportAgent.class,
        "Processed payment for customer: " + customerId + ", amount: " + paymentAmount);

    if (notificationService != null) {
      notificationService.notifyPaymentProcessed(customerId, paymentAmount);
      notificationService.notifyAnalyticsUpdated();
    }

    // Generate transaction ID
    String transactionId = TransactionIdGenerator.generate();

    // Clear cache
    if (context != null) {
      context.remove("customer_" + customerId);
    }

    Map<String, Object> data = new HashMap<>();
    data.put("transactionId", transactionId);
    data.put("amount", paymentAmount);
    data.put("newBalance", newBalance);
    data.put("timestamp", LocalDateTime.now().toString());

    result.put("success", true);
    result.put("data", data);
    result.put("message", "Payment processed successfully");
    return result;
  }

  /**
   * Tool 3: Create support ticket.
   *
   * @param customerId the customer ID
   * @param subject ticket subject
   * @param description ticket description
   * @param priority ticket priority
   * @param context tool context
   * @return ticket creation result
   */
  public Map<String, Object> createTicket(
      String customerId,
      String subject,
      String description,
      String priority,
      Map<String, Object> context) {
    Map<String, Object> result = new HashMap<>();

    // Validation
    if (!ValidationUtils.isValidCustomerId(customerId)) {
      result.put("success", false);
      result.put("error", "Invalid customer ID");
      return result;
    }

    if (subject == null || subject.trim().isEmpty()) {
      result.put("success", false);
      result.put("error", "Subject is required");
      return result;
    }

    if (description == null || description.trim().isEmpty()) {
      result.put("success", false);
      result.put("error", "Description is required");
      return result;
    }

    if (!ValidationUtils.isValidPriority(priority)) {
      result.put("success", false);
      result.put("error", "Invalid priority. Must be: low, medium, high, or urgent");
      return result;
    }

    // Check customer exists
    if (!customerRepository.existsById(customerId)) {
      result.put("success", false);
      result.put("error", "Customer not found");
      return result;
    }

    // Create ticket
    String ticketId = TransactionIdGenerator.generateTicketId();
    Ticket ticket = new Ticket();
    ticket.setTicketId(ticketId);
    ticket.setCustomerId(customerId);
    ticket.setSubject(ValidationUtils.sanitize(subject));
    ticket.setDescription(ValidationUtils.sanitize(description));
    ticket.setPriority(priority.toLowerCase());
    ticket.setStatus("open");
    ticket.setCreated(LocalDateTime.now());

    ticketRepository.save(ticket);

    if (notificationService != null) {
      notificationService.notifyTicketCreated(customerId, ticketId);
      notificationService.notifyAnalyticsUpdated();
    }

    result.put("success", true);
    result.put("data", ticketToMap(ticket));
    result.put("message", "Ticket created successfully");
    return result;
  }

  /**
   * Tool 4: Get tickets for a customer.
   *
   * @param customerId the customer ID
   * @param status filter by status (optional, use "all" for all)
   * @param context tool context
   * @return list of tickets
   */
  public Map<String, Object> getTickets(
      String customerId, String status, Map<String, Object> context) {
    Map<String, Object> result = new HashMap<>();

    // Validation
    if (!ValidationUtils.isValidCustomerId(customerId)) {
      result.put("success", false);
      result.put("error", "Invalid customer ID");
      return result;
    }

    if (!ValidationUtils.isValidStatus(status)) {
      result.put("success", false);
      result.put("error", "Invalid status. Must be: open, closed, pending, or all");
      return result;
    }

    // Check customer exists
    if (!customerRepository.existsById(customerId)) {
      result.put("success", false);
      result.put("error", "Customer not found");
      return result;
    }

    // Filter tickets
    String normalizedStatus = status.toLowerCase();
    List<Ticket> tickets;
    if (normalizedStatus.equals("all")) {
      tickets = ticketRepository.findByCustomerId(customerId);
    } else {
      tickets = ticketRepository.findByCustomerIdAndStatus(customerId, normalizedStatus);
    }

    List<Map<String, Object>> ticketMaps =
        tickets.stream().map(this::ticketToMap).collect(Collectors.toList());

    result.put("success", true);
    result.put("data", ticketMaps);
    result.put("count", ticketMaps.size());
    return result;
  }

  /**
   * Tool 5: Update account settings.
   *
   * @param customerId the customer ID
   * @param email new email (optional, null to keep current)
   * @param tier new tier (optional, null to keep current)
   * @param context tool context
   * @return update result
   */
  public Map<String, Object> updateAccountSettings(
      String customerId, String email, String tier, Map<String, Object> context) {
    Map<String, Object> result = new HashMap<>();

    // Validation
    if (!ValidationUtils.isValidCustomerId(customerId)) {
      result.put("success", false);
      result.put("error", "Invalid customer ID");
      return result;
    }

    if (email == null && tier == null) {
      result.put("success", false);
      result.put("error", "At least one field (email or tier) must be provided");
      return result;
    }

    if (email != null && !ValidationUtils.isValidEmail(email)) {
      result.put("success", false);
      result.put("error", "Invalid email format");
      return result;
    }

    if (tier != null && !ValidationUtils.isValidTier(tier)) {
      result.put("success", false);
      result.put("error", "Invalid tier. Must be: basic, premium, or enterprise");
      return result;
    }

    // Get customer
    Optional<Customer> customerOpt = customerRepository.findById(customerId);
    if (customerOpt.isEmpty()) {
      result.put("success", false);
      result.put("error", "Customer not found");
      return result;
    }

    Customer customer = customerOpt.get();

    // Update fields
    Map<String, String> updates = new HashMap<>();
    if (email != null) {
      customer.setEmail(email);
      updates.put("email", email);
    }
    if (tier != null) {
      String normalizedTier = tier.substring(0, 1).toUpperCase() + tier.substring(1).toLowerCase();
      customer.setTier(normalizedTier);
      updates.put("tier", normalizedTier);
    }

    customerRepository.save(customer);

    // Clear cache
    if (context != null) {
      context.remove("customer_" + customerId);
    }

    result.put("success", true);
    result.put("data", updates);
    result.put("message", "Account settings updated successfully");
    return result;
  }

  /**
   * Tool 6: Validate refund eligibility.
   *
   * @param customerId the customer ID
   * @param context tool context
   * @return eligibility result
   */
  public Map<String, Object> validateRefundEligibility(
      String customerId, Map<String, Object> context) {
    Map<String, Object> result = new HashMap<>();

    // Validation
    if (!ValidationUtils.isValidCustomerId(customerId)) {
      result.put("success", false);
      result.put("error", "Invalid customer ID");
      return result;
    }

    // Get customer
    Optional<Customer> customerOpt = customerRepository.findById(customerId);
    if (customerOpt.isEmpty()) {
      result.put("success", false);
      result.put("error", "Customer not found");
      return result;
    }

    Customer customer = customerOpt.get();

    // Check eligibility criteria
    LocalDateTime accountCreated = customer.getAccountCreated();
    long daysSinceCreation = ChronoUnit.DAYS.between(accountCreated, LocalDateTime.now());
    String status = customer.getStatus();

    boolean eligible = daysSinceCreation <= 30 && "active".equals(status);

    Map<String, Object> data = new HashMap<>();
    data.put("eligible", eligible);
    data.put("daysSinceCreation", daysSinceCreation);
    data.put("accountStatus", status);

    if (eligible) {
      data.put("message", "Customer is eligible for refund");
      // Store validation state
      if (context != null) {
        context.put("refund_validated_" + customerId, true);
      }
    } else {
      if (daysSinceCreation > 30) {
        data.put("message", "Account is older than 30 days");
      } else {
        data.put("message", "Account status is not active");
      }
    }

    result.put("success", true);
    result.put("data", data);
    return result;
  }

  /**
   * Tool 7: Process refund.
   *
   * @param customerId the customer ID
   * @param amount refund amount
   * @param context tool context
   * @return refund result
   */
  public Map<String, Object> processRefund(
      String customerId, Double amount, Map<String, Object> context) {
    Map<String, Object> result = new HashMap<>();

    // Validation
    if (!ValidationUtils.isValidCustomerId(customerId)) {
      result.put("success", false);
      result.put("error", "Invalid customer ID");
      return result;
    }

    // Check validation state
    if (context == null || !Boolean.TRUE.equals(context.get("refund_validated_" + customerId))) {
      result.put("success", false);
      result.put("error", "Refund eligibility must be validated first");
      return result;
    }

    if (amount == null) {
      result.put("success", false);
      result.put("error", "Amount is required");
      return result;
    }

    double refundAmount = ValidationUtils.roundAmount(amount);

    if (!ValidationUtils.isValidAmount(refundAmount)) {
      result.put("success", false);
      result.put("error", "Amount must be between 0 and 100000");
      return result;
    }

    // Get customer
    Optional<Customer> customerOpt = customerRepository.findById(customerId);
    if (customerOpt.isEmpty()) {
      result.put("success", false);
      result.put("error", "Customer not found");
      return result;
    }

    Customer customer = customerOpt.get();

    // Check sufficient balance
    double currentBalance = customer.getBalance();
    if (currentBalance < refundAmount) {
      result.put("success", false);
      result.put("error", "Insufficient balance for refund");
      return result;
    }

    // Process refund
    double newBalance = ValidationUtils.roundAmount(currentBalance - refundAmount);
    customer.setBalance(newBalance);
    customerRepository.save(customer);

    String refundId = TransactionIdGenerator.generateRefundId();

    // Clear cache and validation state
    if (context != null) {
      context.remove("customer_" + customerId);
      context.remove("refund_validated_" + customerId);
    }

    Map<String, Object> data = new HashMap<>();
    data.put("refundId", refundId);
    data.put("amount", refundAmount);
    data.put("newBalance", newBalance);
    data.put("processedAt", LocalDateTime.now().toString());
    data.put("estimatedArrival", "5-7 business days");

    result.put("success", true);
    result.put("data", data);
    result.put("message", "Refund processed successfully");
    return result;
  }

  private Map<String, Object> customerToMap(Customer customer) {
    Map<String, Object> map = new HashMap<>();
    map.put("customerId", customer.getCustomerId());
    map.put("name", customer.getName());
    map.put("email", customer.getEmail());
    map.put("tier", customer.getTier());
    map.put("balance", customer.getBalance());
    map.put("accountCreated", customer.getAccountCreated());
    map.put("status", customer.getStatus());
    return map;
  }

  private Map<String, Object> ticketToMap(Ticket ticket) {
    Map<String, Object> map = new HashMap<>();
    map.put("ticketId", ticket.getTicketId());
    map.put("customerId", ticket.getCustomerId());
    map.put("subject", ticket.getSubject());
    map.put("description", ticket.getDescription());
    map.put("priority", ticket.getPriority());
    map.put("status", ticket.getStatus());
    map.put("created", ticket.getCreated().toString());
    return map;
  }
}
