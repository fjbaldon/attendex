package com.github.fjbaldon.attendex.platform.event;

import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.NonNull;

import java.util.List;

interface SessionRepository extends CrudRepository<Session, Long> {

    @Override
    @NonNull
    List<Session> findAllById(@NonNull Iterable<Long> ids);
}
