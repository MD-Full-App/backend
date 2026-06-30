package com.rustam.modern_dentistry.util.specification.patientinfo;

import com.rustam.modern_dentistry.dao.entity.patient_info.PatientReport;
import com.rustam.modern_dentistry.dto.request.criteria.PatientReportCriteria;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class PatientReportSpecification {

    public static Specification<PatientReport> getReports(PatientReportCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getOperationName() != null && !criteria.getOperationName().isEmpty()) {
                predicates.add(cb.like(root.get("operationName"), "%" + criteria.getOperationName() + "%"));
            }
            if (criteria.getTeethNo() != null) {
                predicates.add(cb.equal(root.get("teethNo"), criteria.getTeethNo()));
            }
            if (criteria.getStartDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("executionDate"), criteria.getStartDate()));
            }
            if (criteria.getEndDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("executionDate"), criteria.getEndDate()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}