package com.github.fjbaldon.attendex.platform.event;

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
import java.util.stream.Collectors;

class InMemoryEventRepository implements EventRepository {
    private final Map<Long, Event> db = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);

    @Override
    @NonNull
    public <S extends Event> S save(@NonNull S entity) {
        if (entity.getId() == null) {
            setId(entity, sequence.incrementAndGet());
        }
        db.put(entity.getId(), entity);
        return entity;
    }

    @Override
    @NonNull
    public <S extends Event> Iterable<S> saveAll(@NonNull Iterable<S> entities) {
        entities.forEach(this::save);
        return entities;
    }

    @Override
    @NonNull
    public Optional<Event> findById(@NonNull Long id) {
        return Optional.ofNullable(db.get(id));
    }

    @Override
    public boolean existsById(@NonNull Long id) {
        return db.containsKey(id);
    }

    @Override
    @NonNull
    public Iterable<Event> findAll() {
        return new ArrayList<>(db.values());
    }

    @Override
    @NonNull
    public Iterable<Event> findAllById(@NonNull Iterable<Long> ids) {
        List<Event> results = new ArrayList<>();
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
    public void delete(@NonNull Event entity) {
        db.remove(entity.getId());
    }

    @Override
    public void deleteAllById(@NonNull Iterable<? extends Long> ids) {
        ids.forEach(this::deleteById);
    }

    @Override
    public void deleteAll(@NonNull Iterable<? extends Event> entities) {
        entities.forEach(this::delete);
    }

    @Override
    public void deleteAll() {
        db.clear();
    }

    @Override
    @NonNull
    public Iterable<Event> findAll(@NonNull Sort sort) {
        return findAll();
    }

    @Override
    @NonNull
    public Page<Event> findAll(@NonNull Pageable pageable) {
        return new PageImpl<>(new ArrayList<>(db.values()), pageable, db.size());
    }

    @Override
    public Page<Event> findAllByOrganizationId(Long organizationId, Pageable pageable) {
        List<Event> orgEvents = db.values().stream()
                .filter(event -> event.getOrganizationId().equals(organizationId))
                .collect(Collectors.toList());
        return new PageImpl<>(orgEvents, pageable, orgEvents.size());
    }

    @Override
    public Optional<Event> findByIdAndOrganizationId(Long id, Long organizationId) {
        return Optional.ofNullable(db.get(id))
                .filter(event -> event.getOrganizationId().equals(organizationId));
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
