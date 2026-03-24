package org.example.transactionriskmonitor.application.query.service;

import org.example.transactionriskmonitor.application.query.dto.TransactionSearchCriteria;
import org.example.transactionriskmonitor.application.query.dto.TransactionSearchResponse;

import java.util.List;

public interface TransactionQueryService {
    List<TransactionSearchResponse> findTransactionsByCriteria(TransactionSearchCriteria criteria);
}
