package com.github.fjbaldon.attendex.platform.audit;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

interface AuditRepository extends PagingAndSortingRepository<Audit, Long>, CrudRepository<Audit, Long> {
}
