package org.playground.pipe.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.playground.pipe.model.Message;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;
import java.util.Objects;

public class MessageEncoder implements Encoder.Text<Message> {

    private static final Logger LOG = LogManager.getLogger();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String encode(Message message) throws EncodeException {
        Objects.requireNonNull(message); //TODO: sostituire con precondition geocall
        LOG.trace("Encoding {}", message);
        try {
            String result = mapper.writeValueAsString(message);
            LOG.trace("Encoded message is {}", result);
            return result;
        } catch (JsonProcessingException e) {
            throw new EncodeException(message, "serialization error", e);
        }
    }

    @Override
    public void init(EndpointConfig config) {
        // nothing to do
    }

    @Override
    public void destroy() {
        // nothing to do
    }
}
