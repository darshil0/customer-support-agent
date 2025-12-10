package com.example.support;

import com.google.adk.agents.BaseAgent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentConfiguration {

  @Bean
  public BaseAgent customerSupportAgent() {
    return CustomerSupportAgent.createAgent();
  }
}
