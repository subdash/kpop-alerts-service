package org.example.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.example.TicketMasterAPI;
import org.example.mapping.model.success.TicketMasterEventSearchApiResponse;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class EventSearchHandler implements RequestHandler<Void, String> {

    private static final JsonMapper jackson = JsonMapper
            .builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(StreamReadFeature.AUTO_CLOSE_SOURCE)
            .build();

    @Override
    public String handleRequest(Void input, Context context) {
        try {
            String response = TicketMasterAPI.eventSearch(10);
            context.getLogger().log(response);
            TicketMasterEventSearchApiResponse parsed = jackson.readValue(response, TicketMasterEventSearchApiResponse.class);
            context.getLogger().log("Parse success!");
            return response;
        } catch (MalformedURLException | JsonProcessingException | URISyntaxException e) {
            context.getLogger().log("Event search API call failed: " + e.getMessage());
        }
        return "{}";
    }
}
