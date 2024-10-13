package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.common.io.CharStreams;
import org.example.mapping.model.success.TicketMasterEventSearchApiResponse;

import java.io.*;
import java.net.*;

public class TicketMasterAPI {

    /**
     * TODO:
     * - test error response/parsing
     * - handle rate limit errors
     * - read API key from env
     */

    private static final String BASE_URL = "https://app.ticketmaster.com";
    private static final String EVENT_SEARCH_ROUTE = "/discovery/v2/events.json";
    private static final String API_KEY = "ArczcVznMlEZoupJgqHpkWuo1ASxGEGA";
    private static final JsonMapper jackson = JsonMapper
            .builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(StreamReadFeature.AUTO_CLOSE_SOURCE)
            .build();

    public static void main(String[] args) {
        try {
            String response = eventSearch(10);
            TicketMasterEventSearchApiResponse parsed = jackson.readValue(response, TicketMasterEventSearchApiResponse.class);
            assert parsed != null;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    // https://developer.ticketmaster.com/products-and-docs/apis/discovery-api/v2/#search-events-v2
    public static String eventSearch(int limit) throws MalformedURLException, URISyntaxException {
        final URI requestUri = new URI(String.format(
                // Hardcode Kpop classification id and state code of TX
                "%s%s?size=%s&apikey=%s&classificationId=KZazBEonSMnZfZ7vkE1&stateCode=TX",
                BASE_URL, EVENT_SEARCH_ROUTE, limit, API_KEY
        ));
        final URL requestUrl = requestUri.toURL();
        HttpURLConnection conn;
        try {
            conn = (HttpURLConnection) requestUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setAllowUserInteraction(false);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                return CharStreams.toString(reader);
            } catch (IOException e) {
                final InputStream errorStream = conn.getErrorStream();
                if (errorStream == null) {
                    // Propagate exception to outer handler:
                    System.err.println("Request failed, but error stream was null");
                    return null;
                }
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream))) {
                    return CharStreams.toString(reader);
                }
            }
        } catch (IOException e) {
            System.err.println("Request failed: " + e.getMessage());
            return null;
        }
    }
}
