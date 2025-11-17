package com.github.fjbaldon.attendex.platform.event;

import org.springframework.lang.NonNull;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

class InMemorySessionRepository implements SessionRepository {
    private final Map<Long, Session> db = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);

    @Override
    @NonNull
    public <S extends Session> S save(@NonNull S entity) {
        if (entity.getId() == null) {
            setId(entity, sequence.incrementAndGet());
        }
        db.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public List<Session> findSessionsWithEventByIdIn(Set<Long> sessionIds) {
        return db.values().stream()
                .filter(session -> sessionIds.contains(session.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> findSessionIdsByEventIdAndIntent(Long eventId, String intent) {
        return db.values().stream()
                .filter(session -> session.getEvent().getId().equals(eventId) && session.getIntent().equals(intent))
                .map(Session::getId)
                .collect(Collectors.toList());
    }

    // --- Other CrudRepository Methods ---

    @Override
    @NonNull
    public <S extends Session> Iterable<S> saveAll(@NonNull Iterable<S> entities) {
        entities.forEach(this::save);
        return entities;
    }

    @Override
    @NonNull
    public Optional<Session> findById(@NonNull Long id) {
        return Optional.ofNullable(db.get(id));
    }

    @Override
    public boolean existsById(@NonNull Long id) {
        return db.containsKey(id);
    }

    @Override
    @NonNull
    public Iterable<Session> findAll() {
        return new ArrayList<>(db.values());
    }

    @Override
    @NonNull
    public Iterable<Session> findAllById(@NonNull Iterable<Long> ids) {
        List<Session> results = new ArrayList<>();
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
    public void delete(@NonNull Session entity) {
        db.remove(entity.getId());
    }

    @Override
    public void deleteAllById(@NonNull Iterable<? extends Long> ids) {
        ids.forEach(this::deleteById);
    }

    @Override
    public void deleteAll(@NonNull Iterable<? extends Session> entities) {
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
