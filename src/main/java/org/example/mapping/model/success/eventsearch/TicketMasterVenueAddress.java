package org.example.mapping.model.success.eventsearch;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TicketMasterVenueAddress(
        String line1,
        String line2
) {
}
