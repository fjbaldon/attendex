package com.github.fjbaldon.attendex.platform.organization;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.StreamSupport;

class InMemoryOrganizerRepository implements OrganizerRepository {
    private final Map<Long, Organizer> db = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);
    private final InMemoryOrganizationRepository organizationRepository;

    InMemoryOrganizerRepository(InMemoryOrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    @Override
    @NonNull
    public <S extends Organizer> S save(@NonNull S organizer) {
        if (organizer.getId() == null) {
            setId(organizer, sequence.incrementAndGet());
        }
        db.put(organizer.getId(), organizer);
        return organizer;
    }

    @Override
    @NonNull
    public <S extends Organizer> Iterable<S> saveAll(@NonNull Iterable<S> entities) {
        entities.forEach(this::save);
        return entities;
    }

    @Override
    @NonNull
    public Optional<Organizer> findById(@NonNull Long id) {
        return Optional.ofNullable(db.get(id));
    }

    @Override
    public boolean existsById(@NonNull Long id) {
        return db.containsKey(id);
    }

    @Override
    @NonNull
    public Iterable<Organizer> findAll() {
        return new ArrayList<>(db.values());
    }

    @Override
    @NonNull
    public Iterable<Organizer> findAllById(@NonNull Iterable<Long> ids) {
        List<Organizer> results = new ArrayList<>();
        ids.forEach(id -> findById(id).ifPresent(results::add));
        return results;
    }

    @Override
    public long count() {
        return db.size();
    }

    @Override
    public void deleteById(@NonNull Long id) {
        db.remove(id);
    }

    @Override
    public void delete(@NonNull Organizer organizer) {
        db.remove(organizer.getId());
    }

    @Override
    public void deleteAllById(@NonNull Iterable<? extends Long> ids) {
        ids.forEach(this::deleteById);
    }

    @Override
    public void deleteAll(@NonNull Iterable<? extends Organizer> entities) {
        entities.forEach(this::delete);
    }

    @Override
    public void deleteAll() {
        db.clear();
    }

    @Override
    @NonNull
    public Iterable<Organizer> findAll(@NonNull Sort sort) {
        return findAll();
    }

    @Override
    @NonNull
    public Page<Organizer> findAll(@NonNull Pageable pageable) {
        return new PageImpl<>(new ArrayList<>(db.values()), pageable, db.size());
    }

    @Override
    public Optional<Organizer> findByEmail(String email) {
        return db.values().stream()
                .filter(o -> o.getEmail().equals(email))
                .findFirst();
    }

    @Override
    public Optional<Organizer> findByVerificationToken(String token) {
        return db.values().stream()
                .filter(o -> token != null && token.equals(o.getVerificationToken()))
                .findFirst();
    }

    @Override
    public boolean existsByOrganizationName(String organizationName) {
        return StreamSupport.stream(organizationRepository.findAll().spliterator(), false)
                .anyMatch(org -> org.getName().equals(organizationName));
    }

    @Override
    public boolean existsByEmailAndOrganizationId(String email, Long organizationId) {
        return db.values().stream()
                .anyMatch(o -> o.getEmail().equals(email) && o.getOrganization().getId().equals(organizationId));
    }

    @Override
    public long countByOrganizationId(Long organizationId) {
        return db.values().stream()
                .filter(o -> o.getOrganization().getId().equals(organizationId))
                .count();
    }

    private void setId(Object entity, Long id) {
        try {
            Field idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Could not set ID for in-memory repository", e);
        }
    }
}
