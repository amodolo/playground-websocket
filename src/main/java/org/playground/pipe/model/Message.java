package org.playground.pipe.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.playground.pipe.utils.SessionId;

import java.io.Serializable;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = TextMessage.class, name = "MSG"),
        @JsonSubTypes.Type(value = ReplyMessage.class, name = "REPLY") }
)
public interface Message extends Serializable {

    String MSG = "MSG";
    String REPLY = "REPLY";

    String getAction();

    @SuppressWarnings("unused")
    SessionId getSender();

    SessionId getTarget();

    String getContent();

    String getType();
}
