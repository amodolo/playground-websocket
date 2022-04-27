package org.playground.pipe.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.playground.pipe.utils.SessionId;

import java.io.Serializable;
import java.util.Objects;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "action", include = JsonTypeInfo.As.EXISTING_PROPERTY)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TextMessage.class, name = Message.TEXT),
        @JsonSubTypes.Type(value = CancelCallMessage.class, name = Message.CANCEL_CALL),
        @JsonSubTypes.Type(value = CallResponseMessage.class, name = Message.CALL_RESPONSE),
        @JsonSubTypes.Type(value = CallMessage.class, name = Message.CALL),
        @JsonSubTypes.Type(value = ReplyMessage.class, name = Message.REPLY)}
)
public abstract class Message<T> implements Serializable {

    protected T content;
    protected SessionId sender;
    protected SessionId target;

    static final String TEXT = "TEXT";
    static final String CANCEL_CALL = "CANCEL_CALL";
    static final String CALL_RESPONSE = "CALL_RESPONSE";
    static final String CALL = "CALL";
    static final String REPLY = "REPLY";

    public Message(T content, SessionId sender, SessionId target) {
        this.content = content;
        this.sender = sender;
        this.target = target;
    }

    public Message() {
    }

    public abstract String getAction();

    public SessionId getSender() {
        return sender;
    }


    public SessionId getTarget() {
        return target;
    }

    public T getContent() {
        return content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message<?> message = (Message<?>) o;
        return content.equals(message.content) && sender.equals(message.sender) && target.equals(message.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, sender, target);
    }

    @Override
    public String toString() {
        return getClass().getName() + "{" +
                "content='" + content + '\'' +
                ", sender=" + sender +
                ", target=" + target +
                '}';
    }
}
