package com.github.fjbaldon.attendex.platform.organization.dto;

import java.time.LocalDate;

public interface DailyRegistration {
    LocalDate getDate();

    Long getCount();
}
