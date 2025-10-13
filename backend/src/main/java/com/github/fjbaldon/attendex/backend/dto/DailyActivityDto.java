package com.github.fjbaldon.attendex.backend.dto;

import java.time.LocalDate;

public interface DailyActivityDto {
    LocalDate getDate();

    Long getCount();
}
