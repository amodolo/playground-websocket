package org.playground.endpoint;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.Serializable;

/**
 *
 */
@JsonAutoDetect(
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        fieldVisibility = JsonAutoDetect.Visibility.NONE)
public class WebMessage implements Serializable {
    @JsonProperty
    private String type;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String sender;
    @JsonProperty
    private String target;
    @JsonProperty
    private Serializable content;

    /**
     *
     * @return
     */
    public String getType() {
        return type;
    }

    /**
     *
     * @return
     */
    public String getSender() {
        return sender;
    }

    void setSender(String sender) {
        this.sender = sender;
    }

    /**
     *
     * @return
     */
    public String getTarget() {
        return target;
    }

    public Serializable getContent() {
        return content;
    }

    WebMessage newTarget(String target) {
        WebMessage newWebMessage = new WebMessage();
        newWebMessage.type = getType();
        newWebMessage.sender = getSender();
        newWebMessage.target = target;
        newWebMessage.content = getContent();
        return newWebMessage;
    }

    @Override
    public String toString() {
        try {
            return toJson();
        } catch (JsonProcessingException e) {
            return "invalid message";
        }
    }

    /**
     *
     * @return
     * @throws JsonProcessingException
     */
    public String toJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }
}
