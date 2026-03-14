package org.example.transactionriskmonitor.application.adapter.out.persistence.mapper;

import org.example.transactionriskmonitor.application.adapter.out.persistence.entity.TransactionJpaEntity;
import org.example.transactionriskmonitor.domain.model.*;
import org.springframework.stereotype.Component;

import java.util.Currency;

@Component
public class TransactionPersistenceMapper {

    public TransactionJpaEntity toEntity(Transaction transaction) {
        return new TransactionJpaEntity(
                transaction.id().value(),
                transaction.accountId().value(),
                transaction.merchantId().value(),
                transaction.money().amount(),
                transaction.money().currency().getCurrencyCode(),
                transaction.country().name(),
                transaction.occurredAt()
        );
    }

    public Transaction toDomain(TransactionJpaEntity entity) {
        return new Transaction(
                new TransactionId(entity.getTransactionId()),
                new AccountId(entity.getAccountId()),
                new MerchantId(entity.getMerchantId()),
                new Money(entity.getAmount(), Currency.getInstance(entity.getCurrency())),
                new Country(entity.getCountry()),
                entity.getOccurredAt()
        );
    }
}
