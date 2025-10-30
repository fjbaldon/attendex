package com.github.fjbaldon.attendex.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventAttendeeSyncResponse {
    private Long attendeeId;
    private String uniqueIdentifier;
    private String qrCodeHash;
    private String firstName;
    private String lastName;
}
