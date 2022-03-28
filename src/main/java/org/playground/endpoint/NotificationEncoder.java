package org.playground.endpoint;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.playground.models.Notification;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

public class NotificationEncoder implements Encoder.Text<Notification> {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String encode(Notification notification) throws EncodeException {
        try {
            return mapper.writeValueAsString(notification);
        } catch (JsonProcessingException e) {
            throw new EncodeException(notification, "serialization error", e);
        }
    }

    @Override
    public void init(EndpointConfig endpointConfig) {
        // Custom initialization logic
    }

    @Override
    public void destroy() {
        // Close resources
    }
}
