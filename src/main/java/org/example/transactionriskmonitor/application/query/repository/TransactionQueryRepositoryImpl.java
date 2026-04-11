package org.example.transactionriskmonitor.application.query.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.example.transactionriskmonitor.application.adapter.out.persistence.entity.RiskAssessmentJpaEntity;
import org.example.transactionriskmonitor.application.adapter.out.persistence.entity.TransactionJpaEntity;
import org.example.transactionriskmonitor.application.query.ReasonMode;
import org.example.transactionriskmonitor.application.query.dto.TransactionReasonRow;
import org.example.transactionriskmonitor.application.query.dto.TransactionSearchCriteria;
import org.example.transactionriskmonitor.application.query.dto.TransactionSearchResponse;
import org.example.transactionriskmonitor.application.query.dto.TransactionSearchRow;
import org.example.transactionriskmonitor.domain.exception.BadRequestException;
import org.example.transactionriskmonitor.domain.model.RiskReason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.*;

/* Custom repository to:
 * use filters from TransactionSearchCriteria
 * build dynamic SQL query with JPA Criteria API (Java Persistence API)
 * fetch matching transactions
 * and return a paginated response
 */

@Repository
public class TransactionQueryRepositoryImpl implements TransactionQueryRepository {
    private static final Logger log = LoggerFactory.getLogger(TransactionQueryRepositoryImpl.class);

    @PersistenceContext
    private EntityManager entityManager;
    /* Entity manager is a tool that talks to the database (save, update, delete etc.)
     PersistenceContext injects an EntityManager and handles its lifecycle (create, reuse, close) */

    @Override
    public Page<TransactionSearchResponse> findByCriteria(
            TransactionSearchCriteria criteria,
            Pageable pageable
    ) {
        // Criteria API helps to build queries in Java instead of writing SQL
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        /* I am building query here, defining its structure */
        CriteriaQuery<TransactionSearchRow> query = cb.createQuery(TransactionSearchRow.class);
        // Root represents a table (entity)
        Root<TransactionJpaEntity> transaction = query.from(TransactionJpaEntity.class);
        Root<RiskAssessmentJpaEntity> assessment = query.from(RiskAssessmentJpaEntity.class);
        log.debug("Executing transaction search with criteria={}, pageable={}", criteria, pageable);

        /* Predicate is a condition in SQL, boolean expression tree
         * This will be a list of boolean conditions that will be combined into a WHERE clause */
        List<Predicate> predicates = buildPredicates(criteria, cb, query, transaction, assessment);
        log.debug("Built {} predicates for transaction query", predicates.size());

        query.select(cb.construct(
                TransactionSearchRow.class,
                transaction.get("id"),
                transaction.get("transactionId"),
                transaction.get("accountId"),
                assessment.get("riskScore"),
                transaction.get("occurredAt")
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
        TypedQuery<TransactionSearchRow> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());
        log.debug("Pagination applied: offset={}, size={}", pageable.getOffset(), pageable.getPageSize());

        /* This executes the main query */
        List<TransactionSearchRow> rows = typedQuery.getResultList();
        List<String> transactionIds = rows.stream()
                .map(TransactionSearchRow::transactionId)
                .toList();

        Map<String, Set<RiskReason>> reasonsByTransactionId = fetchReasonsByTransactionIds(transactionIds);

        /* This gives me a total number of matching rows */
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<TransactionJpaEntity> countTransaction = countQuery.from(TransactionJpaEntity.class);
        Root<RiskAssessmentJpaEntity> countAssessment = countQuery.from(RiskAssessmentJpaEntity.class);

        List<Predicate> countPredicates = buildPredicates(criteria, cb, countQuery, countTransaction, countAssessment);
        /* Count how many matching rows exists and apply same WHERE conditions */
        countQuery.select(cb.count(countTransaction));
        countQuery.where(countPredicates.toArray(new Predicate[0]));

        Long total = entityManager.createQuery(countQuery).getSingleResult();
        log.debug("Query returned {} results, total={}", rows.size(), total);

        List<TransactionSearchResponse> content = rows.stream()
                .map(row -> new TransactionSearchResponse(
                        row.id(),
                        row.transactionId(),
                        row.accountId(),
                        row.riskScore(),
                        reasonsByTransactionId.getOrDefault(
                                row.transactionId(),
                                EnumSet.noneOf(RiskReason.class)
                        ),
                        row.occurredAt()
                ))
                .toList();

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

    // Generic type <T> used because the logic is used for both main query and the count query which
    // return different types
    private <T> List<Predicate> buildPredicates(
            TransactionSearchCriteria criteria,
            CriteriaBuilder cb,
            CriteriaQuery<T> query,
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

        addReasonPredicates(criteria, cb, query, assessment, predicates);
        return predicates;
    }

    private <T> void addReasonPredicates(
            TransactionSearchCriteria criteria,
            CriteriaBuilder cb,
            CriteriaQuery<T> query,
            Root<RiskAssessmentJpaEntity> assessment,
            List<Predicate> predicates
    ) {
        if (criteria.reasons() == null || criteria.reasons().isEmpty()) {
            return;
        }

        ReasonMode mode = criteria.reasonMode() != null
                ? criteria.reasonMode()
                : ReasonMode.ANY;

        switch (mode) {
            case ANY -> predicates.add(buildAnyReasonsPredicate(criteria, cb, query, assessment));
            case ALL -> predicates.add(buildAllReasonsPredicate(criteria, cb, query, assessment));
            case EXACT -> predicates.add(buildExactReasonsPredicate(criteria, cb, query, assessment));
        }
    }

    /* Subquery is a query inside another query
     Reasons is a collection not a single field. */
    private <T> Predicate buildAnyReasonsPredicate(
            TransactionSearchCriteria criteria,
            CriteriaBuilder cb,
            CriteriaQuery<T> query,
            Root<RiskAssessmentJpaEntity> assessment
    ) {
        Subquery<String> subquery = query.subquery(String.class);
        Root<RiskAssessmentJpaEntity> subAssessment = subquery.from(RiskAssessmentJpaEntity.class);
        SetJoin<RiskAssessmentJpaEntity, RiskReason> reasonJoin = subAssessment.joinSet("reasons");

        // this will return transactionId that satisfy the condition below
        subquery.select(subAssessment.get("transactionId"))
                .where(
                        cb.equal(subAssessment.get("transactionId"), assessment.get("transactionId")),
                        reasonJoin.in(criteria.reasons())
                );

        return cb.exists(subquery);
    }

    private <T> Predicate buildAllReasonsPredicate(
            TransactionSearchCriteria criteria,
            CriteriaBuilder cb,
            CriteriaQuery<T> query,
            Root<RiskAssessmentJpaEntity> assessment
    ) {
        List<Predicate> existsPredicates = new ArrayList<>();

        for (RiskReason reason : criteria.reasons()) {
            Subquery<String> subquery = query.subquery(String.class);
            Root<RiskAssessmentJpaEntity> subAssessment = subquery.from(RiskAssessmentJpaEntity.class);
            SetJoin<RiskAssessmentJpaEntity, RiskReason> reasonJoin = subAssessment.joinSet("reasons");

            subquery.select(subAssessment.get("transactionId"))
                    .where(
                            cb.equal(subAssessment.get("transactionId"), assessment.get("transactionId")),
                            cb.equal(reasonJoin, reason)
                    );

            existsPredicates.add(cb.exists(subquery));
        }

        return cb.and(existsPredicates.toArray(new Predicate[0]));
    }

    private <T> Predicate buildExactReasonsPredicate(
            TransactionSearchCriteria criteria,
            CriteriaBuilder cb,
            CriteriaQuery<T> query,
            Root<RiskAssessmentJpaEntity> assessment
    ) {
        Predicate allRequestedPresent = buildAllReasonsPredicate(criteria, cb, query, assessment);

        Subquery<Long> totalReasonsSubquery = query.subquery(Long.class);
        Root<RiskAssessmentJpaEntity> subAssessment = totalReasonsSubquery.from(RiskAssessmentJpaEntity.class);
        SetJoin<RiskAssessmentJpaEntity, RiskReason> reasonJoin = subAssessment.joinSet("reasons");

        totalReasonsSubquery.select(cb.countDistinct(reasonJoin))
                .where(cb.equal(subAssessment.get("transactionId"), assessment.get("transactionId")));

        // check is the number of reasons in DB equal to the number of requested reasons
        Predicate exactCount = cb.equal(totalReasonsSubquery, (long) criteria.reasons().size());
        return cb.and(allRequestedPresent, exactCount);
    }

    private Map<String, Set<RiskReason>> fetchReasonsByTransactionIds(List<String> transactionIds) {
        if (transactionIds.isEmpty()) {
            return Map.of();
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<TransactionReasonRow> query = cb.createQuery(TransactionReasonRow.class);
        Root<RiskAssessmentJpaEntity> assessment = query.from(RiskAssessmentJpaEntity.class);
        SetJoin<RiskAssessmentJpaEntity, RiskReason> reasonJoin = assessment.joinSet("reasons");

        query.select(cb.construct(
                TransactionReasonRow.class,
                assessment.get("transactionId"),
                reasonJoin
        ));

        query.where(assessment.get("transactionId").in(transactionIds));

        List<TransactionReasonRow> rows = entityManager.createQuery(query).getResultList();
        Map<String, Set<RiskReason>> reasonsByTransactionId = new HashMap<>();

        for (TransactionReasonRow row : rows) {
            reasonsByTransactionId
                    .computeIfAbsent(row.transactionId(), key -> EnumSet.noneOf(RiskReason.class))
                    .add(row.reason());
        }

        return reasonsByTransactionId;
    }
}