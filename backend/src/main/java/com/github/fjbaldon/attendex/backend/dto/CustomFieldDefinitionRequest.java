package com.github.fjbaldon.attendex.backend.dto;

import com.github.fjbaldon.attendex.backend.model.FieldType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CustomFieldDefinitionRequest {

    @NotBlank(message = "Field name cannot be blank")
    private String fieldName;

    @NotNull(message = "Field type must be specified")
    private FieldType fieldType;

    private List<String> options;
}
