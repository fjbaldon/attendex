package com.github.fjbaldon.attendex.backend.mapper;

import com.github.fjbaldon.attendex.backend.dto.EventResponse;
import com.github.fjbaldon.attendex.backend.model.Event;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EventMapper {
    EventResponse toEventResponse(Event event);
}
