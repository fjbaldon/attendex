package com.github.fjbaldon.attendex.platform.analytics;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "analytics_attributebreakdown")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class AttributeBreakdown {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long eventId;
    private String attributeName;
    private String attributeValue;
    private long attendeeCount;

    private AttributeBreakdown(Long eventId, String attributeName, String attributeValue) {
        this.eventId = eventId;
        this.attributeName = attributeName;
        this.attributeValue = attributeValue;
        this.attendeeCount = 1;
    }

    static AttributeBreakdown create(Long eventId, String name, String value) {
        return new AttributeBreakdown(eventId, name, value);
    }

    void incrementCount() {
        this.attendeeCount++;
    }
}
