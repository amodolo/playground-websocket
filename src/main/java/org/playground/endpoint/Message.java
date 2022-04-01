package org.playground.endpoint;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 *
 */
@JsonAutoDetect(
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        fieldVisibility = JsonAutoDetect.Visibility.NONE)
public class Message implements Serializable {
    @JsonProperty
    private String type;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String sender;
    @JsonProperty
    @NotEmpty
    private String target;
    @JsonProperty
    private Serializable content;

    /**
     * @return
     */
    public String getType() {
        return type;
    }

    /**
     * @return
     */
    public String getSender() {
        return sender;
    }

    void setSender(String sender) {
        this.sender = sender;
    }

    /**
     * @return
     */
    public String getTarget() {
        return target;
    }

    /**
     * @return
     */
    public Serializable getContent() {
        return content;
    }

    /**
     * @return
     */
    long getTargetUser() {
        int i = target.indexOf('|');
        if (i != -1) return Long.parseLong(target.substring(0, i));
        else return Long.parseLong(target);
    }

    /**
     * @return
     */
    String getTargetWm() {
        int i = target.indexOf('|');
        if (i != -1) return target.substring(i + 1);
        else return null;
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
     * @return
     * @throws JsonProcessingException
     */
    public String toJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }
}
