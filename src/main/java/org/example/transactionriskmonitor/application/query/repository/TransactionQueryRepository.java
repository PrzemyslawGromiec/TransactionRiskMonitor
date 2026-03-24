package org.example.transactionriskmonitor.application.query.repository;

import org.example.transactionriskmonitor.application.query.dto.TransactionSearchCriteria;
import org.example.transactionriskmonitor.application.query.dto.TransactionSearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface TransactionQueryRepository {
    Page<TransactionSearchResponse> findByCriteria(
            TransactionSearchCriteria criteria,
            Pageable pageable
    );
}