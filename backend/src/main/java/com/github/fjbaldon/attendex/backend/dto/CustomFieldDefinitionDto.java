package com.github.fjbaldon.attendex.backend.dto;

import com.github.fjbaldon.attendex.backend.model.FieldType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CustomFieldDefinitionDto {
    private Long id;
    private String fieldName;
    private FieldType fieldType;
    private List<String> options;
}
