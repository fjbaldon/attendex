package com.github.fjbaldon.attendex.platform.attendee;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
class AttendeeAttributeService {

    private final AttributeRepository attributeRepository;
    private final AttendeeRepository attendeeRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public AttributeDto createAttribute(Long organizationId, CreateAttributeDto dto) {
        try {
            Attribute attribute = Attribute.create(organizationId, dto.name(), dto.type(), dto.options());
            Attribute saved = attributeRepository.save(attribute);
            return toDto(saved);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("An attribute with this name already exists.");
        }
    }

    @Transactional(readOnly = true)
    public List<AttributeDto> findAttributes(Long organizationId) {
        return attributeRepository.findAllByOrganizationId(organizationId).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public AttributeDto updateAttribute(Long organizationId, Long attributeId, String newName, List<String> newOptions) {
        Attribute attribute = attributeRepository.findByIdAndOrganizationId(attributeId, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Attribute not found"));

        if (!attribute.getName().equals(newName)) {
            if (attributeRepository.existsByOrganizationIdAndName(organizationId, newName)) {
                throw new IllegalArgumentException("An attribute with the name '" + newName + "' already exists.");
            }
            attendeeRepository.renameAttributeKey(organizationId, attribute.getName(), newName);
            attribute.setName(newName);
        }

        attribute.updateOptions(newOptions);
        return toDto(attributeRepository.save(attribute));
    }

    @Transactional
    public void deleteAttribute(Long organizationId, Long attributeId) {
        Attribute attribute = attributeRepository.findByIdAndOrganizationId(attributeId, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Attribute not found"));

        attributeRepository.delete(attribute);

        attendeeRepository.removeAttributeFromAllAttendees(organizationId, attribute.getName());

        eventPublisher.publishEvent(new AttributeDeletedEvent(organizationId, attribute.getName()));
    }

    private AttributeDto toDto(Attribute attribute) {
        return new AttributeDto(attribute.getId(), attribute.getName(), attribute.getType(), attribute.getOptions());
    }
}
