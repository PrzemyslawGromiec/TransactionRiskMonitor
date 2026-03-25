package org.example.transactionriskmonitor.application.query;

import org.example.transactionriskmonitor.application.query.dto.TransactionSearchCriteria;
import org.example.transactionriskmonitor.application.query.dto.TransactionSearchResponse;
import org.example.transactionriskmonitor.application.query.service.TransactionQueryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionQueryController {
    private final TransactionQueryService transactionQueryService;

    public TransactionQueryController(TransactionQueryService transactionQueryService) {
        this.transactionQueryService = transactionQueryService;
    }

    @GetMapping("/search")
    public PagedModel<TransactionSearchResponse> searchTransactions(
            @ModelAttribute TransactionSearchCriteria criteria,
            Pageable pageable
    ) {
        Page<TransactionSearchResponse> page = transactionQueryService.findTransactionsByCriteria(criteria, pageable);
        return new PagedModel<>(page);
    }
}
