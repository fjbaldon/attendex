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

class InMemoryScannerRepository implements ScannerRepository {
    private final Map<Long, Scanner> db = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);
    private final InMemoryOrganizationRepository organizationRepository;

    InMemoryScannerRepository(InMemoryOrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    @Override
    @NonNull
    public <S extends Scanner> S save(@NonNull S scanner) {
        if (scanner.getId() == null) {
            setId(scanner, sequence.incrementAndGet());
        }
        db.put(scanner.getId(), scanner);
        return scanner;
    }

    @Override
    @NonNull
    public <S extends Scanner> Iterable<S> saveAll(@NonNull Iterable<S> entities) {
        entities.forEach(this::save);
        return entities;
    }

    @Override
    @NonNull
    public Optional<Scanner> findById(@NonNull Long id) {
        return Optional.ofNullable(db.get(id));
    }

    @Override
    public boolean existsById(@NonNull Long id) {
        return db.containsKey(id);
    }

    @Override
    @NonNull
    public Iterable<Scanner> findAll() {
        return new ArrayList<>(db.values());
    }

    @Override
    @NonNull
    public Iterable<Scanner> findAllById(@NonNull Iterable<Long> ids) {
        List<Scanner> results = new ArrayList<>();
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
    public void delete(@NonNull Scanner entity) {
        db.remove(entity.getId());
    }

    @Override
    public void deleteAllById(@NonNull Iterable<? extends Long> ids) {
        ids.forEach(this::deleteById);
    }

    @Override
    public void deleteAll(@NonNull Iterable<? extends Scanner> entities) {
        entities.forEach(this::delete);
    }

    @Override
    public void deleteAll() {
        db.clear();
    }

    @Override
    @NonNull
    public Iterable<Scanner> findAll(@NonNull Sort sort) {
        return findAll();
    }

    @Override
    @NonNull
    public Page<Scanner> findAll(@NonNull Pageable pageable) {
        return new PageImpl<>(new ArrayList<>(db.values()), pageable, db.size());
    }

    @Override
    public Optional<Scanner> findByEmail(String email) {
        return db.values().stream()
                .filter(s -> s.getEmail().equals(email))
                .findFirst();
    }

    @Override
    public boolean existsByEmailAndOrganizationId(String email, Long organizationId) {
        return db.values().stream()
                .anyMatch(s -> s.getEmail().equals(email) && s.getOrganization().getId().equals(organizationId));
    }

    @Override
    public long countByOrganizationId(Long organizationId) {
        return db.values().stream()
                .filter(s -> s.getOrganization().getId().equals(organizationId))
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
