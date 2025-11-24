package com.github.fjbaldon.attendex.platform.organization;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

interface OrganizationRepository extends CrudRepository<Organization, Long> {

    List<Organization> findByLifecycleAndSubscriptionExpiresAtBefore(String lifecycle, Instant now);

    List<Organization> findBySubscriptionExpiresAtBetweenOrderBySubscriptionExpiresAtAsc(Instant start, Instant end, Pageable pageable);

    List<Organization> findByOrderByIdDesc(Pageable pageable);

    List<Organization> findByLifecycleIn(List<String> lifecycles, Pageable pageable);

    @Query("SELECT o FROM Organization o WHERE " +
            "(:query IS NULL OR LOWER(o.name) LIKE LOWER(CONCAT('%', CAST(:query AS string), '%'))) AND " +
            "(:lifecycle IS NULL OR o.lifecycle = :lifecycle) AND " +
            "(:subscriptionType IS NULL OR o.subscriptionType = :subscriptionType)")
    Page<Organization> searchAndFilter(
            @Param("query") String query,
            @Param("lifecycle") String lifecycle,
            @Param("subscriptionType") String subscriptionType,
            Pageable pageable
    );

    @Query(value = "SELECT CAST(created_at AS DATE) as date, COUNT(id) as count " +
            "FROM organization_organization " +
            "WHERE created_at >= :startDate " +
            "GROUP BY date " +
            "ORDER BY date ", nativeQuery = true)
    List<DailyRegistration> findDailyRegistrationsSince(@Param("startDate") Instant startDate);

    long countByLifecycle(String lifecycle);

    long countBySubscriptionType(String subscriptionType);
}
