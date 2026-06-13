package com.example.support.service;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

  @Autowired private SimpMessagingTemplate messagingTemplate;

  public void notifyTicketCreated(String customerId, String ticketId) {
    messagingTemplate.convertAndSend(
        "/topic/tickets",
        Map.of("type", "TICKET_CREATED", "customerId", customerId, "ticketId", ticketId));
  }

  public void notifyPaymentProcessed(String customerId, double amount) {
    messagingTemplate.convertAndSend(
        "/topic/payments",
        Map.of("type", "PAYMENT_PROCESSED", "customerId", customerId, "amount", amount));
  }

  public void notifyAnalyticsUpdated() {
    messagingTemplate.convertAndSend("/topic/analytics", Map.of("type", "ANALYTICS_UPDATED"));
  }
}
