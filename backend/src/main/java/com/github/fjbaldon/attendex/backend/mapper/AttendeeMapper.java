package com.github.fjbaldon.attendex.backend.mapper;

import com.github.fjbaldon.attendex.backend.dto.AttendeeRequest;
import com.github.fjbaldon.attendex.backend.dto.AttendeeResponse;
import com.github.fjbaldon.attendex.backend.model.Attendee;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AttendeeMapper {
    AttendeeResponse toAttendeeResponse(Attendee attendee);

    Attendee toAttendee(AttendeeRequest attendeeRequest);
}
