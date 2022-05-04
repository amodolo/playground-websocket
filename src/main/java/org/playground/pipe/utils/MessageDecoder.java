package org.playground.pipe.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.playground.pipe.model.Message;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

/**
 *
 */

@SuppressWarnings("rawtypes")
public class MessageDecoder implements Decoder.Text<Message> {

    private static final Logger LOG = LogManager.getLogger();
    protected static final String DESERIALIZATION_ERROR = "deserialization error";
    private final ObjectMapper mapper;

    @SuppressWarnings("unused")
    public MessageDecoder() {
        this(new ObjectMapper());
    }

    /**
     * For test purposes only.
     *
     * @param mapper {@link ObjectMapper} instance.
     */
    private MessageDecoder(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Message<?> decode(String s) throws DecodeException {
        try {
            LOG.trace("Decoding {}", s);
            Message<?> result = mapper.readValue(s, Message.class);
            LOG.trace("Decoded message is {}", result);
            return result;
        } catch (JsonProcessingException e) {
            throw new DecodeException(s, DESERIALIZATION_ERROR, e);
        }
    }

    @Override
    public boolean willDecode(String s) {
        return (s != null); //TODO e le stringhe vuote o non valide?
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
