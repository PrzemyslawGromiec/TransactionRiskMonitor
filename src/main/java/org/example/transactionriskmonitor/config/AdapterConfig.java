package org.example.transactionriskmonitor.config;

import org.example.transactionriskmonitor.application.adapter.out.memory.*;
import org.example.transactionriskmonitor.application.port.out.*;
import org.example.transactionriskmonitor.domain.model.RiskPolicy;
import org.example.transactionriskmonitor.domain.service.RiskScorer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class AdapterConfig {

    @Bean
    public VelocityPort velocityPort() {
        return new InMemoryVelocityAdapter(Duration.ofMinutes(5));
    }

    @Bean
    public LocationHistoryPort locationHistoryPort() {
        return new InMemoryLocationHistoryAdapter(Duration.ofMinutes(10));
    }

    @Bean
    public MerchantHistoryPort merchantHistoryPort() {
        return new InMemoryMerchantHistoryAdapter();
    }

    @Bean
    public AlertPublisherPort alertPublisherPort() {
        return new ConsoleAlertPublisher();
    }

    @Bean
    public RiskPolicy riskPolicy() {
        return RiskPolicy.defaultPolicy();
    }

    @Bean
    public RiskScorer riskScorer(RiskPolicy riskPolicy) {
        return new RiskScorer(riskPolicy);
    }
}
