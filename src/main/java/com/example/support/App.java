package com.example.support;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Map;
import java.util.List;

/**
 * Main application entry point and REST API controller.
 */
@SpringBootApplication
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class App {

    @Autowired
    private CustomerSupportAgent agent;

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @GetMapping("/health")
    public Map<String, Object> healthCheck() {
        return Map.of(
            "status", "UP",
            "message", "Customer Support API is running",
            "version", "1.0.4"
        );
    }

    @GetMapping("/customer/{customerId}")
    public Map<String, Object> getCustomer(@PathVariable String customerId) {
        return agent.getCustomerAccount(customerId, null);
    }

    @PutMapping("/account")
    public Map<String, Object> updateAccount(@RequestBody Map<String, String> request) {
        return agent.updateAccountSettings(
            request.get("customerId"),
            request.get("email"),
            request.get("tier"),
            null
        );
    }

    @PostMapping("/payment")
    public Map<String, Object> processPayment(@RequestBody Map<String, Object> request) {
        return agent.processPayment(
            (String) request.get("customerId"),
            request.get("amount"),
            null
        );
    }

    @PostMapping("/ticket")
    public Map<String, Object> createTicket(@RequestBody Map<String, String> request) {
        return agent.createTicket(
            request.get("customerId"),
            request.get("subject"),
            request.get("description"),
            request.get("priority"),
            null
        );
    }

    @GetMapping("/tickets/{customerId}")
    public Map<String, Object> getTickets(
            @PathVariable String customerId,
            @RequestParam(defaultValue = "all") String status) {
        return agent.getTickets(customerId, status, null);
    }

    @PostMapping("/refund/validate")
    public Map<String, Object> validateRefund(@RequestBody Map<String, String> request) {
        return agent.validateRefundEligibility(request.get("customerId"), null);
    }

    @PostMapping("/refund/process")
    public Map<String, Object> processRefund(@RequestBody Map<String, Object> request) {
        return agent.processRefund(
            (String) request.get("customerId"),
            request.get("amount"),
            null
        );
    }
}
