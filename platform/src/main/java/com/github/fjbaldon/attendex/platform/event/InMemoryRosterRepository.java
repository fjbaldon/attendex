package com.github.fjbaldon.attendex.platform.event;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

class InMemoryRosterRepository implements RosterRepository {
    private final Map<RosterEntryId, RosterEntry> db = new ConcurrentHashMap<>();

    @Override
    public Page<Long> findAttendeeIdsByEventId(Long eventId, Pageable pageable) {
        List<Long> attendeeIds = db.values().stream()
                .filter(entry -> entry.getId().getEventId().equals(eventId))
                .map(entry -> entry.getId().getAttendeeId())
                .collect(Collectors.toList());
        return new PageImpl<>(attendeeIds, pageable, attendeeIds.size());
    }

    @Override
    public long countByEventId(Long eventId) {
        return db.values().stream().filter(entry -> entry.getId().getEventId().equals(eventId)).count();
    }

    @Override
    @NonNull
    public <S extends RosterEntry> S save(@NonNull S entity) {
        db.put(entity.getId(), entity);
        return entity;
    }

    @Override
    @NonNull
    public <S extends RosterEntry> Iterable<S> saveAll(@NonNull Iterable<S> entities) {
        entities.forEach(this::save);
        return entities;
    }

    @Override
    @NonNull
    public Optional<RosterEntry> findById(@NonNull RosterEntryId rosterEntryId) {
        return Optional.ofNullable(db.get(rosterEntryId));
    }

    @Override
    public boolean existsById(@NonNull RosterEntryId rosterEntryId) {
        return db.containsKey(rosterEntryId);
    }

    @Override
    @NonNull
    public Iterable<RosterEntry> findAll() {
        return new ArrayList<>(db.values());
    }

    @Override
    @NonNull
    public Iterable<RosterEntry> findAllById(@NonNull Iterable<RosterEntryId> rosterEntryIds) {
        List<RosterEntry> results = new ArrayList<>();
        rosterEntryIds.forEach(id -> findById(id).ifPresent(results::add));
        return results;
    }

    @Override
    public long count() {
        return db.size();
    }

    @Override
    public void deleteById(@NonNull RosterEntryId rosterEntryId) {
        db.remove(rosterEntryId);
    }

    @Override
    public void delete(@NonNull RosterEntry entity) {
        db.remove(entity.getId());
    }

    @Override
    public void deleteAllById(@NonNull Iterable<? extends RosterEntryId> rosterEntryIds) {
        rosterEntryIds.forEach(this::deleteById);
    }

    @Override
    public void deleteAll(@NonNull Iterable<? extends RosterEntry> entities) {
        entities.forEach(this::delete);
    }

    @Override
    public void deleteAll() {
        db.clear();
    }

    @Override
    @NonNull
    public Iterable<RosterEntry> findAll(@NonNull Sort sort) {
        return findAll();
    }

    @Override
    @NonNull
    public Page<RosterEntry> findAll(@NonNull Pageable pageable) {
        return new PageImpl<>(new ArrayList<>(db.values()), pageable, db.size());
    }
}
