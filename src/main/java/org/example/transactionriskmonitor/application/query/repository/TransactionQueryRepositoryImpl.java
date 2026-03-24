package org.example.transactionriskmonitor.application.query.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.example.transactionriskmonitor.application.adapter.out.persistence.entity.RiskAssessmentJpaEntity;
import org.example.transactionriskmonitor.application.adapter.out.persistence.entity.TransactionJpaEntity;
import org.example.transactionriskmonitor.application.query.dto.TransactionSearchCriteria;
import org.example.transactionriskmonitor.application.query.dto.TransactionSearchResponse;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class TransactionQueryRepositoryImpl implements TransactionQueryRepository{

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<TransactionSearchResponse> findByCriteria(TransactionSearchCriteria criteria) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<TransactionSearchResponse> query = cb.createQuery(TransactionSearchResponse.class);

        Root<TransactionJpaEntity> transaction = query.from(TransactionJpaEntity.class);
        Root<RiskAssessmentJpaEntity> assessment = query.from(RiskAssessmentJpaEntity.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(transaction.get("transactionId"), assessment.get("transactionId")));

        if (criteria.transactionId() != null && !criteria.transactionId().isBlank()) {
            predicates.add(cb.equal(transaction.get("transactionId"), criteria.transactionId()));
        }

        if (criteria.accountId() != null && !criteria.accountId().isBlank()) {
            predicates.add(cb.equal(transaction.get("accountId"), criteria.accountId()));
        }

        if (criteria.reason() != null && !criteria.reason().isBlank()) {
            Expression<String> reasonsWithCommas =
                    cb.concat(cb.concat(",", assessment.get("reasons")), ",");

            predicates.add(cb.like(reasonsWithCommas, "%," + criteria.reason() + ",%"));
        }

        if (criteria.minRiskScore() != null) {
            predicates.add(cb.greaterThanOrEqualTo(
                    assessment.get("riskScore"),
                    criteria.minRiskScore()
            ));
        }

        if (criteria.maxRiskScore() != null) {
            predicates.add(cb.lessThanOrEqualTo(
                    assessment.get("riskScore"),
                    criteria.maxRiskScore()
            ));
        }

        if (criteria.occurredFrom() != null) {
            predicates.add(cb.greaterThanOrEqualTo(
                    transaction.get("occurredAt"),
                    criteria.occurredFrom()
            ));
        }

        if (criteria.occurredTo() != null) {
            predicates.add(cb.lessThanOrEqualTo(
                    transaction.get("occurredAt"),
                    criteria.occurredTo()
            ));
        }

        query.select(cb.construct(
                TransactionSearchResponse.class,
                transaction.get("id"),
                transaction.get("transactionId"),
                transaction.get("accountId"),
                assessment.get("riskScore")
        ));

        query.where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(query).getResultList();
    }
}
