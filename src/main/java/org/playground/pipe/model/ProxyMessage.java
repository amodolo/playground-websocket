package org.playground.pipe.model;

import org.playground.pipe.utils.SessionId;

public class ProxyMessage<T> extends Message<T> {

    private final Message<T> message;
    private final SessionId sender;


    public ProxyMessage(Message<T> message, SessionId sender) {
        super(message.getContent(), sender, message.getTarget());
        this.message = message;
        this.sender = sender;
    }

    @Override
    public String getAction() {
        return message.getAction();
    }

    @Override
    public SessionId getSender() {
        return sender;
    }

    @Override
    public SessionId getTarget() {
        return message.getTarget();
    }

    @Override
    public T getContent() {
        return message.getContent();
    }

    @Override
    public String toString() {
        return "ProxyMessage{" +
                "message=" + message +
                ", sender=" + sender +
                '}';
    }
}
