package org.example.transactionriskmonitor.application.query.repository;

import org.example.transactionriskmonitor.application.query.dto.TransactionSearchCriteria;
import org.example.transactionriskmonitor.application.query.dto.TransactionSearchResponse;

import java.util.List;

public interface TransactionQueryRepository {
    List<TransactionSearchResponse> findByCriteria(TransactionSearchCriteria criteria);
}
