package org.playground.pipe.model;

import org.playground.pipe.utils.SessionId;

public class ProxyMessage implements Message {

    private final Message message;
    private final SessionId sender;

    public ProxyMessage(Message message, SessionId sender) {
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
    public String getContent() {
        return message.getContent();
    }

    @Override
    public String getType() {
        return message.getType();
    }
}
