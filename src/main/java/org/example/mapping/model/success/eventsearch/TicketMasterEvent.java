package org.example.mapping.model.success.eventsearch;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TicketMasterEvent(
        String name,
        String type,
        String url,
        TicketMasterDates dates,
        String info,
        List<TicketMasterPriceRange> priceRanges,
        TicketMasterVenuesWrapper _embedded
) {
}
