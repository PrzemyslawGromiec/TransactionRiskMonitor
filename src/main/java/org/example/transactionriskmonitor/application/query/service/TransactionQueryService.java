package org.example.transactionriskmonitor.application.query.service;

import org.example.transactionriskmonitor.application.query.dto.TransactionSearchCriteria;
import org.example.transactionriskmonitor.application.query.dto.TransactionSearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface TransactionQueryService {
    Page<TransactionSearchResponse> findTransactionsByCriteria(
            TransactionSearchCriteria criteria,
            Pageable pageable
    );
}
