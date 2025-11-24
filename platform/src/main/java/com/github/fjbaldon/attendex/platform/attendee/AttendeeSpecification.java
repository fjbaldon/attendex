package com.github.fjbaldon.attendex.platform.attendee;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class AttendeeSpecification {

    static Specification<Attendee> withFilters(Long organizationId, String query, Map<String, String> attributeFilters) {
        return (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Organization Scope
            predicates.add(cb.equal(root.get("organizationId"), organizationId));

            // 2. Soft Delete Scope
            predicates.add(cb.isNull(root.get("deletedAt")));

            // 3. Text Search (Identity OR Name)
            if (query != null && !query.isBlank()) {
                String likePattern = "%" + query.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("identity")), likePattern),
                        cb.like(cb.lower(root.get("firstName")), likePattern),
                        cb.like(cb.lower(root.get("lastName")), likePattern)
                ));
            }

            // 4. Dynamic Attributes (JSONB)
            if (attributeFilters != null) {
                attributeFilters.forEach((key, value) -> {
                    if (value != null && !value.isBlank()) {
                        // Use PostgreSQL function to extract JSON text
                        // jsonb_extract_path_text(attributes, key)
                        Expression<String> jsonExtract = cb.function(
                                "jsonb_extract_path_text",
                                String.class,
                                root.get("attributes"),
                                cb.literal(key)
                        );
                        predicates.add(cb.equal(jsonExtract, value));
                    }
                });
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
