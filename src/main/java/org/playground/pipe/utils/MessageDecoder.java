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
public class MessageDecoder implements Decoder.Text<Message> {

    private static final Logger LOG = LogManager.getLogger();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Message decode(String s) throws DecodeException {
        try {
            LOG.trace("Decoding {}", s);
            return mapper.readValue(s, Message.class);
        } catch (JsonProcessingException e) {
            throw new DecodeException(s, "deserialization error", e);
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
