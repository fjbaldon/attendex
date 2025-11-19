package com.github.fjbaldon.attendex.platform.organization;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.time.Instant;
import java.util.List;

interface OrganizationRepository extends CrudRepository<Organization, Long> {
    List<Organization> findBySubscriptionExpiresAtBetweenOrderBySubscriptionExpiresAtAsc(Instant start, Instant end, Pageable pageable);

    List<Organization> findByOrderByIdDesc(Pageable pageable);

    List<Organization> findByLifecycleIn(List<String> lifecycles, Pageable pageable);

    List<Organization> findAll(Pageable pageable);
}
