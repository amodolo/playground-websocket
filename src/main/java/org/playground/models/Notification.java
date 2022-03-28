package org.playground.models;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.Serializable;

@JsonAutoDetect(
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        fieldVisibility = JsonAutoDetect.Visibility.NONE)
public class Notification implements Serializable {
    @JsonProperty
    private String type;
    @JsonProperty
    private Long sender;
    @JsonProperty
    private String target;
    @JsonProperty
    private Serializable content;

    public String getType() {
        return type;
    }

    public Long getSender() {
        return sender;
    }

    public String getTarget() {
        return target;
    }

    public Long getReceiver() {
        int idx = target.indexOf("@");
        if (idx != -1) return Long.parseLong(target.substring(0, idx));
        else return Long.parseLong(target);
    }

    public String getReceiverWM() {
        int idx = target.indexOf("@");
        if (idx != -1) return target.substring(idx+1);
        else return null;
    }

    public Serializable getContent() {
        return content;
    }

    @Override
    public String toString() {
        try {
            return toJson();
        } catch (JsonProcessingException e) {
            return "invalid message";
        }
    }

    public String toJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }
    public static Notification fromJson(String s) throws JsonProcessingException {
        return new ObjectMapper().readValue(s, Notification.class);
    }
}
