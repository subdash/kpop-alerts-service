package org.example.mapping.model.success.eventsearch;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TicketMasterVenuesWrapper(List<TicketMasterVenue> venues) {

}
