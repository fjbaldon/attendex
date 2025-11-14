package com.github.fjbaldon.attendex.platform.attendee;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

interface AttributeRepository extends CrudRepository<Attribute, Long> {
    List<Attribute> findAllByOrganizationId(Long organizationId);

    boolean existsByOrganizationIdAndName(Long organizationId, String name);
}
