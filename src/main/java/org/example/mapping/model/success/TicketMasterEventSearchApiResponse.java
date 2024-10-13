package org.example.mapping.model.success;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.example.mapping.model.success.eventsearch.TicketMasterEventsWrapper;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TicketMasterEventSearchApiResponse(
        TicketMasterEventsWrapper _embedded
) {

}
