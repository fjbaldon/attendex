package com.github.fjbaldon.attendex.platform.admin;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

interface StewardRepository extends PagingAndSortingRepository<Steward, Long>, CrudRepository<Steward, Long> {

    boolean existsByEmail(String email);

    Optional<Steward> findByEmail(String email);
}
