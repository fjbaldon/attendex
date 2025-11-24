package com.github.fjbaldon.attendex.platform.organization;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
class OrganizationQueryService {

    private final OrganizationRepository organizationRepository;

    @Transactional(readOnly = true)
    public OrganizationDto findOrganizationById(Long organizationId) {
        return organizationRepository.findById(organizationId)
                .map(Organization::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found"));
    }

    @Transactional(readOnly = true)
    public List<OrganizationDto> findExpiringSubscriptions(Instant expirationThreshold, Pageable pageable) {
        return organizationRepository.findBySubscriptionExpiresAtBetweenOrderBySubscriptionExpiresAtAsc(
                Instant.now(),
                expirationThreshold,
                pageable
        ).stream().map(Organization::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrganizationDto> findRecentRegistrations(Pageable pageable) {
        return organizationRepository.findByOrderByIdDesc(pageable)
                .stream().map(Organization::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrganizationDto> findOrganizationsByLifecycle(List<String> lifecycles, Pageable pageable) {
        return organizationRepository.findByLifecycleIn(lifecycles, pageable)
                .stream().map(Organization::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DailyRegistration> getDailyRegistrations(Instant startDate) {
        return organizationRepository.findDailyRegistrationsSince(startDate);
    }

    @Transactional(readOnly = true)
    public long countByLifecycle(String lifecycle) {
        return organizationRepository.countByLifecycle(lifecycle);
    }

    @Transactional(readOnly = true)
    public long countBySubscriptionType(String subscriptionType) {
        return organizationRepository.countBySubscriptionType(subscriptionType);
    }

    @Transactional(readOnly = true)
    public Page<OrganizationDto> findAllOrganizations(String query, String lifecycle, String subscriptionType, Pageable pageable) {
        String safeQuery = (query != null && !query.isBlank()) ? query.trim() : null;
        String safeLifecycle = (lifecycle != null && !lifecycle.isBlank() && !lifecycle.equals("ALL")) ? lifecycle : null;
        String safeSubscription = (subscriptionType != null && !subscriptionType.isBlank() && !subscriptionType.equals("ALL")) ? subscriptionType : null;

        return organizationRepository.searchAndFilter(safeQuery, safeLifecycle, safeSubscription, pageable)
                .map(Organization::toDto);
    }
}
