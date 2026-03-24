package org.example.transactionriskmonitor.application.query.service;

import org.example.transactionriskmonitor.application.query.dto.TransactionSearchCriteria;
import org.example.transactionriskmonitor.application.query.dto.TransactionSearchResponse;
import org.example.transactionriskmonitor.application.query.repository.TransactionQueryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionQueryServiceImpl implements TransactionQueryService{
    private final TransactionQueryRepository transactionQueryRepository;
    private final TransactionSearchCriteriaValidator validator;

    public TransactionQueryServiceImpl(TransactionQueryRepository transactionQueryRepository, TransactionSearchCriteriaValidator validator) {
        this.transactionQueryRepository = transactionQueryRepository;
        this.validator = validator;
    }

    @Override
    public List<TransactionSearchResponse> findTransactionsByCriteria(TransactionSearchCriteria criteria) {
        validator.validate(criteria);
        return transactionQueryRepository.findByCriteria(criteria);
    }
}
