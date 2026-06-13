package com.example.support.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
public class Ticket {
  @Id private String ticketId;
  private String customerId;
  private String subject;

  @Column(length = 1000)
  private String description;

  private String priority;
  private String status;
  private LocalDateTime created;

  public Ticket() {}

  public Ticket(
      String ticketId,
      String customerId,
      String subject,
      String description,
      String priority,
      String status,
      LocalDateTime created) {
    this.ticketId = ticketId;
    this.customerId = customerId;
    this.subject = subject;
    this.description = description;
    this.priority = priority;
    this.status = status;
    this.created = created;
  }

  // Getters and Setters
  public String getTicketId() {
    return ticketId;
  }

  public void setTicketId(String ticketId) {
    this.ticketId = ticketId;
  }

  public String getCustomerId() {
    return customerId;
  }

  public void setCustomerId(String customerId) {
    this.customerId = customerId;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getPriority() {
    return priority;
  }

  public void setPriority(String priority) {
    this.priority = priority;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public LocalDateTime getCreated() {
    return created;
  }

  public void setCreated(LocalDateTime created) {
    this.created = created;
  }
}
