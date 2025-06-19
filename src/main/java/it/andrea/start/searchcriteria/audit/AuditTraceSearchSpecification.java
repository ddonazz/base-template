package it.andrea.start.searchcriteria.audit;

import java.io.Serial;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;

import it.andrea.start.constants.AuditActivity;
import it.andrea.start.constants.AuditTypeOperation;
import it.andrea.start.models.audit.AuditTrace;
import it.andrea.start.utils.HelperQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class AuditTraceSearchSpecification implements Specification<AuditTrace> {

    @Serial
    private static final long serialVersionUID = -6128605859804781607L;

    private final AuditTraceSearchCriteria criteria;

    public AuditTraceSearchSpecification(AuditTraceSearchCriteria criteria) {
        this.criteria = criteria;
    }

    @Override
    public Predicate toPredicate(@NonNull Root<AuditTrace> root, CriteriaQuery<?> query, @NonNull CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicatesAnd = new ArrayList<>();

        final Long id = criteria.getId();
        final String sessionId = criteria.getSessionId();
        final AuditActivity activity = criteria.getActivity();
        final Long userId = criteria.getUserId();
        final String userName = criteria.getUserName();
        final AuditTypeOperation auditType = criteria.getAuditType();
        final String textSearch = criteria.getTextSearch();
        final Instant dateFrom = criteria.getDateFrom();
        final Instant dateTo = criteria.getDateTo();

        if (id != null) {
            predicatesAnd.add(criteriaBuilder.equal(root.get("id"), id));
        }
        if (StringUtils.isNotBlank(sessionId)) {
            predicatesAnd.add(criteriaBuilder.equal(root.get("sessionId"), sessionId));
        }
        if (activity != null) {
            predicatesAnd.add(criteriaBuilder.equal(root.get("activity"), activity));
        }
        if (userId != null) {
            predicatesAnd.add(criteriaBuilder.equal(root.get("userId"), userId));
        }
        if (StringUtils.isNotBlank(userName)) {
            predicatesAnd.add(criteriaBuilder.equal(root.get("userName"), userName));
        }
        if (auditType != null) {
            predicatesAnd.add(criteriaBuilder.equal(root.get("auditType"), auditType));
        }
        if (StringUtils.isNotBlank(textSearch)) {
            List<Predicate> predicatesOr = new ArrayList<>();

            String value = HelperQuery.prepareForLikeQuery(textSearch); 
            predicatesOr.add(criteriaBuilder.like(criteriaBuilder.upper(root.get("controllerMethod")), value));
            predicatesOr.add(criteriaBuilder.like(criteriaBuilder.upper(root.get("entityName")), value));
            predicatesOr.add(criteriaBuilder.like(criteriaBuilder.upper(root.get("entityKeyValue")), value));
            predicatesOr.add(criteriaBuilder.like(criteriaBuilder.upper(root.get("entityOldValue")), value));
            predicatesOr.add(criteriaBuilder.like(criteriaBuilder.upper(root.get("entityNewValue")), value));
            predicatesOr.add(criteriaBuilder.like(criteriaBuilder.upper(root.get("httpContextRequest")), value));
            predicatesOr.add(criteriaBuilder.like(criteriaBuilder.upper(root.get("httpContextResponse")), value));
            predicatesOr.add(criteriaBuilder.like(criteriaBuilder.upper(root.get("exceptionTrace")), value));

            Predicate orPredicate = criteriaBuilder.or(predicatesOr.toArray(new Predicate[0]));
            predicatesAnd.add(orPredicate);
        }
        if (dateFrom != null) {
            predicatesAnd.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dateEvent"), dateFrom));
        }
        if (dateTo != null) {
            predicatesAnd.add(criteriaBuilder.lessThanOrEqualTo(root.get("dateEvent"), dateTo));
        }

        return criteriaBuilder.and(predicatesAnd.toArray(new Predicate[0]));
    }

}
