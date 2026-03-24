package org.example.transactionriskmonitor.application.query;

import org.example.transactionriskmonitor.application.query.dto.TransactionSearchCriteria;
import org.example.transactionriskmonitor.application.query.dto.TransactionSearchResponse;
import org.example.transactionriskmonitor.application.query.service.TransactionQueryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionQueryController {
    private final TransactionQueryService transactionQueryService;

    public TransactionQueryController(TransactionQueryService transactionQueryService) {
        this.transactionQueryService = transactionQueryService;
    }

    @GetMapping("/search")
    public Page<TransactionSearchResponse> searchTransactions(
            @ModelAttribute TransactionSearchCriteria criteria,
            Pageable pageable
    ) {
        return transactionQueryService.findTransactionsByCriteria(criteria, pageable);
    }
}
