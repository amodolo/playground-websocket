package org.playground.pipe.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.playground.pipe.utils.Pipe;

import java.io.Serializable;
import java.util.Objects;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "action", include = JsonTypeInfo.As.EXISTING_PROPERTY)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TextMessage.class, name = Message.TEXT),
        @JsonSubTypes.Type(value = CancelCallMessage.class, name = Message.CANCEL_CALL),
        @JsonSubTypes.Type(value = CallResponseMessage.class, name = Message.CALL_RESPONSE),
        @JsonSubTypes.Type(value = CallMessage.class, name = Message.CALL),
        @JsonSubTypes.Type(value = ReplyMessage.class, name = Message.REPLY),
        @JsonSubTypes.Type(value = ErrorMessage.class, name = Message.ERROR),
        @JsonSubTypes.Type(value = ErrorMessage.class, name = Message.INITIALIZED)
})
public abstract class Message<T> implements Serializable {

    protected T content;
    protected Pipe sender;
    protected Pipe target;

    static final String TEXT = "TEXT";
    static final String CANCEL_CALL = "CANCEL_CALL";
    static final String CALL_RESPONSE = "CALL_RESPONSE";
    static final String CALL = "CALL";
    static final String REPLY = "REPLY";
    static final String ERROR = "ERROR";
    static final String INITIALIZED = "INITIALIZED";

    protected Message() {
    }

    protected Message(T content, Pipe sender, Pipe target) {
        this.content = content;
        this.sender = sender;
        this.target = target;
    }

    public abstract String getAction();

    public Pipe getSender() {
        return sender;
    }


    public Pipe getTarget() {
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
        return content.equals(message.content) && sender.equals(message.sender) && target.equals(message.target) && getAction().equals(message.getAction());
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, sender, target, getAction());
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
