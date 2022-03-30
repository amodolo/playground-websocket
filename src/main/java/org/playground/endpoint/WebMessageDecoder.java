package org.playground.endpoint;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

/**
 *
 */
public class WebMessageDecoder implements Decoder.Text<WebMessage> {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public WebMessage decode(String s) throws DecodeException {
        try {
            return mapper.readValue(s, WebMessage.class);
        } catch (JsonProcessingException e) {
            throw new DecodeException(s, "deserialization error", e);
        }
    }

    @Override
    public boolean willDecode(String s) {
        return (s != null);
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
