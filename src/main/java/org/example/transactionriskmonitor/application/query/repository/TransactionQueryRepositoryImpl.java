package org.example.transactionriskmonitor.application.query.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.example.transactionriskmonitor.application.adapter.out.persistence.entity.RiskAssessmentJpaEntity;
import org.example.transactionriskmonitor.application.adapter.out.persistence.entity.TransactionJpaEntity;
import org.example.transactionriskmonitor.application.query.dto.TransactionSearchCriteria;
import org.example.transactionriskmonitor.application.query.dto.TransactionSearchResponse;
import org.example.transactionriskmonitor.domain.exception.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/* This approach is used as it's:
 * - hexagonal architecture
 * - dependency inversion
 * - custom query
 */

@Repository
public class TransactionQueryRepositoryImpl implements TransactionQueryRepository {
    private static final Logger log = LoggerFactory.getLogger(TransactionQueryRepositoryImpl.class);

    @PersistenceContext
    private EntityManager entityManager;
    /* Entity manager is a tool that talks to the database (save, update, delete etc.)
     * PersistenceContext injects an EntityManager and handles its lifecycle (create, reuse, close)
     */

    @Override
    public Page<TransactionSearchResponse> findByCriteria(
            TransactionSearchCriteria criteria,
            Pageable pageable
    ) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        /* I am building query here, defining its structure */
        CriteriaQuery<TransactionSearchResponse> query = cb.createQuery(TransactionSearchResponse.class);
        Root<TransactionJpaEntity> transaction = query.from(TransactionJpaEntity.class);
        Root<RiskAssessmentJpaEntity> assessment = query.from(RiskAssessmentJpaEntity.class);
        log.debug("Executing transaction search with criteria={}, pageable={}", criteria, pageable);

        /* Predicate is a condition in SQL, boolean expression tree
         * This will be a list of boolean conditions that will be combined into a WHERE clause */
        List<Predicate> predicates = buildPredicates(criteria, cb, transaction, assessment);
        log.debug("Built {} predicates for transaction query", predicates.size());

        query.select(cb.construct(
                TransactionSearchResponse.class,
                transaction.get("id"),
                transaction.get("transactionId"),
                transaction.get("accountId"),
                assessment.get("riskScore")
        ));

        /* Java needs to convert List into Predicate[] for where()
         * new Predicate[0] as JVM creates correctly sized array internally */
        query.where(predicates.toArray(new Predicate[0]));

        if (pageable.getSort().isSorted()) {
            query.orderBy(buildOrders(cb, transaction, assessment, pageable.getSort()));
        } else {
            query.orderBy(cb.desc(transaction.get("occurredAt")));
        }

        /* This creates executable query that will interact with DB */
        TypedQuery<TransactionSearchResponse> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());
        log.debug("Pagination applied: offset={}, size={}", pageable.getOffset(), pageable.getPageSize());

        /* This executes the main query */
        List<TransactionSearchResponse> content = typedQuery.getResultList();
        /* This gives me a total number of matching rows */
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<TransactionJpaEntity> countTransaction = countQuery.from(TransactionJpaEntity.class);
        Root<RiskAssessmentJpaEntity> countAssessment = countQuery.from(RiskAssessmentJpaEntity.class);

        List<Predicate> countPredicates = buildPredicates(criteria, cb, countTransaction, countAssessment);
        /* Count how many matching rows exists and apply same WHERE conditions */
        countQuery.select(cb.count(countTransaction));
        countQuery.where(countPredicates.toArray(new Predicate[0]));

        Long total = entityManager.createQuery(countQuery).getSingleResult();
        log.debug("Query returned {} results, total={}", content.size(), total);
        
        return new PageImpl<>(content, pageable, total);
    }

    private List<Order> buildOrders(
            CriteriaBuilder cb,
            Root<TransactionJpaEntity> transaction,
            Root<RiskAssessmentJpaEntity> assessment,
            Sort sort
    ) {
        List<Order> orders = new ArrayList<>();

        for (Sort.Order sortOrder : sort) {
            String property = sortOrder.getProperty();
            boolean ascending = sortOrder.isAscending();

            Path<?> path = switch (property) {
                case "id" -> transaction.get("id");
                case "transactionId" -> transaction.get("transactionId");
                case "accountId" -> transaction.get("accountId");
                case "occurredAt" -> transaction.get("occurredAt");
                case "riskScore" -> assessment.get("riskScore");
                default -> throw new BadRequestException("Unsupported sort field: " + property);
            };

            orders.add(ascending ? cb.asc(path) : cb.desc(path));
        }

        return orders;
    }

    private List<Predicate> buildPredicates(
            TransactionSearchCriteria criteria,
            CriteriaBuilder cb,
            Root<TransactionJpaEntity> transaction,
            Root<RiskAssessmentJpaEntity> assessment
    ) {
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

        return predicates;
    }
}