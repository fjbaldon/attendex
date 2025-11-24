package com.github.fjbaldon.attendex.platform.organization;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrganizationFacade {

    private final OrganizationIngestService ingestService;
    private final OrganizationQueryService queryService;
    private final MemberService memberService;

    // --- ORGANIZATION INGEST ---

    @Transactional
    public OrganizationDto registerOrganization(RegistrationRequestDto request) {
        return ingestService.registerOrganization(request);
    }

    @Transactional
    public void verifyOrganizer(String token) {
        ingestService.verifyOrganizer(token);
    }

    @Transactional
    public OrganizationDto updateLifecycle(Long organizationId, String lifecycle) {
        return ingestService.updateLifecycle(organizationId, lifecycle);
    }

    @Transactional
    public OrganizationDto updateSubscription(Long organizationId, String subscriptionType, Instant expiresAt) {
        return ingestService.updateSubscription(organizationId, subscriptionType, expiresAt);
    }

    @Transactional
    public OrganizationDto updateOrganizationDetails(Long organizationId, UpdateOrganizationDetailsDto dto) {
        return ingestService.updateOrganizationDetails(organizationId, dto);
    }

    // --- ORGANIZATION QUERY ---

    @Transactional(readOnly = true)
    public OrganizationDto findOrganizationById(Long organizationId) {
        return queryService.findOrganizationById(organizationId);
    }

    @Transactional(readOnly = true)
    public List<OrganizationDto> findExpiringSubscriptions(Instant expirationThreshold, Pageable pageable) {
        return queryService.findExpiringSubscriptions(expirationThreshold, pageable);
    }

    @Transactional(readOnly = true)
    public List<OrganizationDto> findRecentRegistrations(Pageable pageable) {
        return queryService.findRecentRegistrations(pageable);
    }

    @Transactional(readOnly = true)
    public List<OrganizationDto> findOrganizationsByLifecycle(List<String> lifecycles, Pageable pageable) {
        return queryService.findOrganizationsByLifecycle(lifecycles, pageable);
    }

    @Transactional(readOnly = true)
    public List<DailyRegistration> getDailyRegistrations(Instant startDate) {
        return queryService.getDailyRegistrations(startDate);
    }

    @Transactional(readOnly = true)
    public long countByLifecycle(String lifecycle) {
        return queryService.countByLifecycle(lifecycle);
    }

    @Transactional(readOnly = true)
    public long countBySubscriptionType(String subscriptionType) {
        return queryService.countBySubscriptionType(subscriptionType);
    }

    @Transactional(readOnly = true)
    public Page<OrganizationDto> findAllOrganizations(String query, String lifecycle, String subscriptionType, Pageable pageable) {
        return queryService.findAllOrganizations(query, lifecycle, subscriptionType, pageable);
    }

    // --- ORGANIZERS ---

    @Transactional
    public OrganizerDto createOrganizer(Long organizationId, CreateUserRequestDto request) {
        return memberService.createOrganizer(organizationId, request);
    }

    @Transactional(readOnly = true)
    public Page<OrganizerDto> findOrganizers(Long organizationId, String query, Pageable pageable) {
        return memberService.findOrganizers(organizationId, query, pageable);
    }

    @Transactional
    public void deleteOrganizer(Long organizationId, Long organizerIdToDelete, String currentOrganizerEmail) {
        memberService.deleteOrganizer(organizationId, organizerIdToDelete, currentOrganizerEmail);
    }

    // --- SCANNERS ---

    @Transactional
    public ScannerDto createScanner(Long organizationId, CreateUserRequestDto request) {
        return memberService.createScanner(organizationId, request);
    }

    @Transactional(readOnly = true)
    public Page<ScannerDto> findScanners(Long organizationId, String query, Pageable pageable) {
        return memberService.findScanners(organizationId, query, pageable);
    }

    @Transactional
    public ScannerDto toggleScannerStatus(Long organizationId, Long scannerId) {
        return memberService.toggleScannerStatus(organizationId, scannerId);
    }

    @Transactional
    public void deleteScanner(Long organizationId, Long scannerId) {
        memberService.deleteScanner(organizationId, scannerId);
    }

    @Transactional(readOnly = true)
    public Map<Long, String> getScannerEmailsByIds(Set<Long> scannerIds) {
        return memberService.getScannerEmailsByIds(scannerIds);
    }

    @Transactional(readOnly = true)
    public Optional<ScannerAuthDto> findScannerAuthByEmail(String email) {
        return memberService.findScannerAuthByEmail(email);
    }

    // --- SHARED MEMBER AUTH ---

    @Transactional(readOnly = true)
    public Optional<UserAuthDto> findUserAuthByEmail(String email) {
        return memberService.findUserAuthByEmail(email);
    }

    @Transactional
    public void changeUserPassword(String email, String newPassword) {
        memberService.changeUserPassword(email, newPassword);
    }

    @Transactional
    public void resetUserPassword(Long organizationId, Long userId, String newPassword) {
        memberService.resetUserPassword(organizationId, userId, newPassword);
    }
}
