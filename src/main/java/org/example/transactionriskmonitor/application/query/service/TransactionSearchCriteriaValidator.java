package org.example.transactionriskmonitor.application.query.service;

import org.example.transactionriskmonitor.application.query.dto.TransactionSearchCriteria;
import org.example.transactionriskmonitor.domain.exception.BadRequestException;
import org.springframework.stereotype.Component;

@Component
public class TransactionSearchCriteriaValidator {

    public void validate(TransactionSearchCriteria criteria) {
        if (criteria.minRiskScore() != null
                && criteria.maxRiskScore() != null
                && criteria.minRiskScore() > criteria.maxRiskScore()) {
            throw new BadRequestException("minRiskScore cannot be greater than maxRiskScore");
        }
    }
}
