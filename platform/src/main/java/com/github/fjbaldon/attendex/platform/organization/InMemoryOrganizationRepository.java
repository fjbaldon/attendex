package com.github.fjbaldon.attendex.platform.organization;

import org.springframework.lang.NonNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

class InMemoryOrganizationRepository implements OrganizationRepository {
    private final Map<Long, Organization> db = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);

    @Override
    @NonNull
    public <S extends Organization> S save(@NonNull S organization) {
        if (organization.getId() == null) {
            setId(organization, sequence.incrementAndGet());
        }
        db.put(organization.getId(), organization);
        return organization;
    }

    @Override
    @NonNull
    public <S extends Organization> Iterable<S> saveAll(@NonNull Iterable<S> entities) {
        entities.forEach(this::save);
        return entities;
    }

    @Override
    @NonNull
    public Optional<Organization> findById(@NonNull Long id) {
        return Optional.ofNullable(db.get(id));
    }

    @Override
    public boolean existsById(@NonNull Long id) {
        return db.containsKey(id);
    }

    @Override
    @NonNull
    public Iterable<Organization> findAll() {
        return new ArrayList<>(db.values());
    }

    @Override
    @NonNull
    public Iterable<Organization> findAllById(@NonNull Iterable<Long> ids) {
        List<Organization> results = new ArrayList<>();
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
    public void delete(@NonNull Organization entity) {
        db.remove(entity.getId());
    }

    @Override
    public void deleteAllById(@NonNull Iterable<? extends Long> ids) {
        ids.forEach(this::deleteById);
    }

    @Override
    public void deleteAll(@NonNull Iterable<? extends Organization> entities) {
        entities.forEach(this::delete);
    }

    @Override
    public void deleteAll() {
        db.clear();
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
