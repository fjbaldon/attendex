package com.github.fjbaldon.attendex.backend.mapper;

import com.github.fjbaldon.attendex.backend.dto.ActiveEventResponse;
import com.github.fjbaldon.attendex.backend.dto.EventAttendeeSyncResponse;
import com.github.fjbaldon.attendex.backend.model.Event;
import com.github.fjbaldon.attendex.backend.model.EventAttendee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AppMapper {

    ActiveEventResponse toActiveEventResponse(Event event);

    @Mapping(source = "attendee.id", target = "attendeeId")
    @Mapping(source = "attendee.schoolIdNumber", target = "schoolIdNumber")
    EventAttendeeSyncResponse toEventAttendeeSyncResponse(EventAttendee eventAttendee);
}
