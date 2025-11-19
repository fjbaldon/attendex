package com.github.fjbaldon.attendex.platform.attendee;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.util.Assert;

import java.util.List;

@Entity
@Table(name = "attendee_attribute")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class Attribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long organizationId;
    private String name;
    private String type;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> options;

    private Attribute(Long organizationId, String name, String type, List<String> options) {
        Assert.notNull(organizationId, "Organization ID must not be null");
        Assert.hasText(name, "Attribute name must not be blank");
        Assert.hasText(type, "Attribute type must not be blank");
        this.organizationId = organizationId;
        this.name = name;
        this.type = type;
        this.options = options;
    }

    static Attribute create(Long organizationId, String name, String type, List<String> options) {
        return new Attribute(organizationId, name, type, options);
    }

    void updateOptions(List<String> newOptions) {
        Assert.notEmpty(newOptions, "Options list cannot be empty");
        this.options = newOptions;
    }
}
