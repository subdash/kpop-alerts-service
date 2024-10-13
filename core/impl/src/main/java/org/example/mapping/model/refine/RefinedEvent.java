package org.example.mapping.model.refine;

import org.example.mapping.model.success.eventsearch.TicketMasterEvent;
import org.example.mapping.model.success.eventsearch.TicketMasterPriceRange;
import org.example.mapping.model.success.eventsearch.TicketMasterVenue;

public record RefinedEvent(
        String name,
        String date,
        String time,
        String url,
        Float ticketMin,
        Float ticketMax,
        String city,
        String state,
        String venue
) {

    public static RefinedEvent fromEventPayload(TicketMasterEvent event) {
        TicketMasterVenue venue = event._embedded().venues().getFirst();
        TicketMasterPriceRange ticketMasterPriceRange = event.priceRanges().getFirst();
        return new RefinedEvent(
                event.name(),
                event.dates().start().localDate(),
                event.dates().start().localTime(),
                event.url(),
                ticketMasterPriceRange.min(),
                ticketMasterPriceRange.max(),
                venue.city().name(),
                venue.state().name(),
                venue.name()
        );
    }

    @Override
    public String toString() {
        return String.format(
                "%s - %s @ %s at %s in %s, %s\nTickets from $%.2f to $%.2f\nMore info at %s",
                name,
                date,
                time,
                venue,
                city,
                state,
                ticketMin,
                ticketMax,
                url
        );
    }
}
