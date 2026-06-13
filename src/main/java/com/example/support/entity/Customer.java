package com.example.support.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "customers")
public class Customer {
  @Id private String customerId;
  private String name;
  private String email;
  private String tier;
  private Double balance;
  private LocalDateTime accountCreated;
  private String status;

  public Customer() {}

  public Customer(
      String customerId,
      String name,
      String email,
      String tier,
      Double balance,
      LocalDateTime accountCreated,
      String status) {
    this.customerId = customerId;
    this.name = name;
    this.email = email;
    this.tier = tier;
    this.balance = balance;
    this.accountCreated = accountCreated;
    this.status = status;
  }

  // Getters and Setters
  public String getCustomerId() {
    return customerId;
  }

  public void setCustomerId(String customerId) {
    this.customerId = customerId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getTier() {
    return tier;
  }

  public void setTier(String tier) {
    this.tier = tier;
  }

  public Double getBalance() {
    return balance;
  }

  public void setBalance(Double balance) {
    this.balance = balance;
  }

  public LocalDateTime getAccountCreated() {
    return accountCreated;
  }

  public void setAccountCreated(LocalDateTime accountCreated) {
    this.accountCreated = accountCreated;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
