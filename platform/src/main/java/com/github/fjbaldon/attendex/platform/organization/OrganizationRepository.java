package com.github.fjbaldon.attendex.platform.organization;

import com.github.fjbaldon.attendex.platform.organization.dto.DailyRegistration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

interface OrganizationRepository extends CrudRepository<Organization, Long> {

    List<Organization> findBySubscriptionExpiresAtBetweenOrderBySubscriptionExpiresAtAsc(Instant start, Instant end, Pageable pageable);

    List<Organization> findByOrderByIdDesc(Pageable pageable);

    List<Organization> findByLifecycleIn(List<String> lifecycles, Pageable pageable);

    Page<Organization> findAll(Pageable pageable);

    @Query(value = "SELECT CAST(created_at AS DATE) as date, COUNT(id) as count " +
            "FROM organization_organization " +
            "WHERE created_at >= :startDate " +
            "GROUP BY date " +
            "ORDER BY date ASC", nativeQuery = true)
    List<DailyRegistration> findDailyRegistrationsSince(@Param("startDate") Instant startDate);
}
