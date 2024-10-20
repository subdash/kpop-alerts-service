package org.example.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.example.TicketMasterAPI;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

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
            PublishRequest publishRequest = new PublishRequest();
            publishRequest.setMessage(response);
            publishRequest.setTopicArn(SNS_TOPIC_ARN);
            AmazonSNS snsClient = AmazonSNSClient.builder().build();
            PublishResult publishResult = snsClient.publish(publishRequest);
            context.getLogger().log("Publish result: " + publishResult.toString());
            return response;
        } catch (MalformedURLException | URISyntaxException e) {
            context.getLogger().log("Event search API call failed: " + e.getMessage());
        }
        return "{}";
    }
}
