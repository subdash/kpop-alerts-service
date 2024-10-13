package org.example.mapping.model.success.eventsearch;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TicketMasterPriceRange(
        String type,
        String currency,
        Float min,
        Float max
) {
}
