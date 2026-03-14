package org.example.transactionriskmonitor.application.adapter.out.persistence;

import org.example.transactionriskmonitor.application.adapter.out.persistence.entity.TransactionJpaEntity;
import org.example.transactionriskmonitor.application.adapter.out.persistence.mapper.TransactionPersistenceMapper;
import org.example.transactionriskmonitor.application.adapter.out.persistence.repository.SpringDataTransactionRepository;
import org.example.transactionriskmonitor.application.port.out.TransactionRepositoryPort;
import org.example.transactionriskmonitor.domain.model.Transaction;
import org.example.transactionriskmonitor.domain.model.TransactionId;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Profile("postgres")
public class JpaTransactionRepositoryAdapter implements TransactionRepositoryPort {

    private final SpringDataTransactionRepository repository;
    private final TransactionPersistenceMapper mapper;

    public JpaTransactionRepositoryAdapter(SpringDataTransactionRepository repository, TransactionPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public boolean exists(TransactionId id) {
        return repository.existsById(id.value());
    }

    @Override
    public Transaction save(Transaction tx) {
        TransactionJpaEntity saved = repository.save(mapper.toEntity(tx));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Transaction> findById(TransactionId id) {
        return repository.findById(id.value())
                .map(mapper::toDomain);
    }
}
