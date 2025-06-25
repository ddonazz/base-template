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
    public Predicate toPredicate(@NonNull Root<AuditTrace> root, @NonNull CriteriaQuery<?> query, @NonNull CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicatesAnd = new ArrayList<>();

        final Long id = criteria.getId();
        final String userName = criteria.getUsername();
        final AuditActivity activity = criteria.getActivity();
        final AuditTypeOperation auditType = criteria.getAuditType();
        final Instant dateEventFrom = criteria.getDateEventFrom(); 
        final Instant dateEventTo = criteria.getDateEventTo();     
        final String className = criteria.getClassName();
        final String methodName = criteria.getMethodName();
        final String controllerMethod = criteria.getControllerMethod();
        final String httpMethod = criteria.getHttpMethod();
        final String requestUri = criteria.getRequestUri();
        final String clientIpAddress = criteria.getClientIpAddress();
        final Boolean success = criteria.getSuccess();
        final Long durationMs = criteria.getDurationMs();
        final String exceptionType = criteria.getExceptionType();
        final String exceptionMessage = criteria.getExceptionMessage();

        if (id != null) {
            predicatesAnd.add(criteriaBuilder.equal(root.get("id"), id));
        }
        if (StringUtils.isNotBlank(userName)) {
            predicatesAnd.add(criteriaBuilder.like(criteriaBuilder.upper(root.get("username")), HelperQuery.prepareForLikeQuery(userName)));
        }
        if (activity != null) {
            predicatesAnd.add(criteriaBuilder.equal(root.get("activity"), activity));
        }
        if (auditType != null) {
            predicatesAnd.add(criteriaBuilder.equal(root.get("auditType"), auditType));
        }

        if (StringUtils.isNotBlank(className)) {
            predicatesAnd.add(criteriaBuilder.like(criteriaBuilder.upper(root.get("className")), HelperQuery.prepareForLikeQuery(className)));
        }
        if (StringUtils.isNotBlank(methodName)) {
            predicatesAnd.add(criteriaBuilder.like(criteriaBuilder.upper(root.get("methodName")), HelperQuery.prepareForLikeQuery(methodName)));
        }
        if (StringUtils.isNotBlank(controllerMethod)) {
            predicatesAnd.add(criteriaBuilder.like(criteriaBuilder.upper(root.get("controllerMethod")), HelperQuery.prepareForLikeQuery(controllerMethod)));
        }
        if (StringUtils.isNotBlank(httpMethod)) {
            predicatesAnd.add(criteriaBuilder.like(criteriaBuilder.upper(root.get("httpMethod")), HelperQuery.prepareForLikeQuery(httpMethod)));
        }
        if (StringUtils.isNotBlank(requestUri)) {
            predicatesAnd.add(criteriaBuilder.like(criteriaBuilder.upper(root.get("requestUri")), HelperQuery.prepareForLikeQuery(requestUri)));
        }
        if (StringUtils.isNotBlank(clientIpAddress)) {
            predicatesAnd.add(criteriaBuilder.like(criteriaBuilder.upper(root.get("clientIpAddress")), HelperQuery.prepareForLikeQuery(clientIpAddress)));
        }
        if (success != null) {
            predicatesAnd.add(criteriaBuilder.equal(root.get("success"), success));
        }
        if (durationMs != null) {
            predicatesAnd.add(criteriaBuilder.equal(root.get("durationMs"), durationMs));
        }
        if (StringUtils.isNotBlank(exceptionType)) {
            predicatesAnd.add(criteriaBuilder.like(criteriaBuilder.upper(root.get("exceptionType")), HelperQuery.prepareForLikeQuery(exceptionType)));
        }
        if (StringUtils.isNotBlank(exceptionMessage)) {
            predicatesAnd.add(criteriaBuilder.like(criteriaBuilder.upper(root.get("exceptionMessage")), HelperQuery.prepareForLikeQuery(exceptionMessage)));
        }

        if (dateEventFrom != null) {
            predicatesAnd.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dateEvent"), dateEventFrom));
        }
        if (dateEventTo != null) {
            predicatesAnd.add(criteriaBuilder.lessThanOrEqualTo(root.get("dateEvent"), dateEventTo));
        }

        return criteriaBuilder.and(predicatesAnd.toArray(new Predicate[0]));
    }}
