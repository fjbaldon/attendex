package com.github.fjbaldon.attendex.platform.admin;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

interface StewardRepository extends CrudRepository<Steward, Long> {

    boolean existsByEmail(String email);

    Optional<Steward> findByEmail(String email);
}
