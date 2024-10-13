package org.example.mapping.model.success.eventsearch;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TicketMasterVenue(
        String name,
        String type,
        String postalCode,
        TicketMasterCity city,
        TicketMasterState state,
        TicketMasterCountry country,
        TicketMasterVenueAddress address
) {
}
