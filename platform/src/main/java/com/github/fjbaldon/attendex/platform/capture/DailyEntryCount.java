package com.github.fjbaldon.attendex.platform.capture;

import java.time.LocalDate;

public interface DailyEntryCount {
    LocalDate getDate();
    Long getCount();
}
