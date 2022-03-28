package org.playground.endpoint;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.playground.models.Notification;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

public class NotificationDecoder implements Decoder.Text<Notification> {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Notification decode(String s) throws DecodeException {
        try {
            return mapper.readValue(s, Notification.class);
        } catch (JsonProcessingException e) {
            throw new DecodeException(s, "deserialization error", e);
        }
    }

    @Override
    public boolean willDecode(String s) {
        return (s != null);
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
