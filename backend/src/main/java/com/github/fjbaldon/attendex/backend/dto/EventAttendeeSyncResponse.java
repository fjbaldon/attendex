package com.github.fjbaldon.attendex.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventAttendeeSyncResponse {
    private Long attendeeId;
    private String schoolIdNumber;
    private String qrCodeHash;
}
