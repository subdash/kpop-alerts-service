package org.example.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.example.TicketMasterAPI;
import org.example.mapping.model.refine.RefinedEvent;
import org.example.mapping.model.success.TicketMasterEventSearchApiResponse;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.stream.Collectors;

public class EventSearchHandler implements RequestHandler<Void, String> {

    private static final String SNS_TOPIC_ARN = System.getenv("SNS_TOPIC_ARN");
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
            String formatted = parsed
                    ._embedded()
                    .events()
                    .stream()
                    .map(RefinedEvent::fromEventPayload)
                    .map(RefinedEvent::toString)
                    .collect(Collectors.joining("\n------------------------\n"));
            PublishResponse publishResponse;
            try (SnsClient snsClient = SnsClient.builder()
                    .region(Region.US_EAST_1)
                    .build()) {
                PublishRequest publishRequest = PublishRequest
                        .builder()
                        .message(formatted)
                        .topicArn(SNS_TOPIC_ARN)
                        .build();
                publishResponse = snsClient.publish(publishRequest);
                context.getLogger().log("Publish result: " + publishResponse.toString());
            } catch (Exception e) {
                context.getLogger().log("Unable to publish event: " + e.getMessage());
            }
            return response;
        } catch (JsonProcessingException | MalformedURLException | URISyntaxException e) {
            context.getLogger().log("Event search API call failed: " + e.getMessage());
        }
        return "{}";
    }
}
