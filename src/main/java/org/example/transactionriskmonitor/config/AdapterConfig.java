package org.example.transactionriskmonitor.config;

import org.example.transactionriskmonitor.application.adapter.out.memory.*;
import org.example.transactionriskmonitor.application.port.out.*;
import org.example.transactionriskmonitor.domain.model.RiskPolicy;
import org.example.transactionriskmonitor.domain.service.RiskScorer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class AdapterConfig {

    @Bean
    public VelocityPort velocityPort(
            @Value("${velocity.window.minutes:5}") long minutes
    ) {
        return new InMemoryVelocityAdapter(Duration.ofMinutes(minutes));
    }

    @Bean
    public LocationHistoryPort locationHistoryPort(
            @Value("${location.history.window.minutes:10}") long minutes
    ) {
        return new InMemoryLocationHistoryAdapter(Duration.ofMinutes(minutes));
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
