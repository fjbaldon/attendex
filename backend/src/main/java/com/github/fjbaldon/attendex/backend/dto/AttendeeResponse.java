package com.github.fjbaldon.attendex.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttendeeResponse {
    private Long id;
    private String schoolIdNumber;
    private String firstName;
    private Character middleInitial;
    private String lastName;
    private String course;
    private Integer yearLevel;
}
