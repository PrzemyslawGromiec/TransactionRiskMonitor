package org.example.transactionriskmonitor.application.query;

import org.example.transactionriskmonitor.application.query.dto.TransactionSearchCriteria;
import org.example.transactionriskmonitor.application.query.dto.TransactionSearchResponse;
import org.example.transactionriskmonitor.application.query.service.TransactionQueryService;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionQueryController {
    private final TransactionQueryService transactionQueryService;

    public TransactionQueryController(TransactionQueryService transactionQueryService) {
        this.transactionQueryService = transactionQueryService;
    }

    @GetMapping("/search")
    public List<TransactionSearchResponse> searchTransactions(
            @RequestParam(required = false) String transactionId,
            @RequestParam(required = false) String accountId,
            @RequestParam(required = false) String reason,
            @RequestParam(required = false) Integer minRiskScore,
            @RequestParam(required = false) Integer maxRiskScore,
            @RequestParam(required = false) Instant occurredFrom,
            @RequestParam(required = false) Instant occurredTo
    ) {
        TransactionSearchCriteria criteria = new TransactionSearchCriteria(
                transactionId,
                accountId,
                reason,
                minRiskScore,
                maxRiskScore,
                occurredFrom,
                occurredTo
        );

        return transactionQueryService.findTransactionsByCriteria(criteria);
    }
}
