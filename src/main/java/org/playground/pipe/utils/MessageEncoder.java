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

@SuppressWarnings("rawtypes")
public class MessageEncoder implements Encoder.Text<Message> {

    private static final Logger LOG = LogManager.getLogger();
    protected static final String SERIALIZATION_ERROR = "serialization error";
    private final ObjectMapper mapper;

    public MessageEncoder() {
        this(new ObjectMapper());
    }

    /**
     * For test purposes only.
     *
     * @param mapper {@link ObjectMapper} instance.
     */
    private MessageEncoder(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public String encode(Message message) throws EncodeException {
        Objects.requireNonNull(message); //TODO: sostituire con precondition geocall
        LOG.trace("Encoding {}", message);
        try {
            String result = mapper.writeValueAsString(message);
            LOG.trace("Encoded message is {}", result);
            return result;
        } catch (JsonProcessingException e) {
            throw new EncodeException(message, SERIALIZATION_ERROR, e);
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
