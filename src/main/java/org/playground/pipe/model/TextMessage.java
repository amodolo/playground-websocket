package org.playground.pipe.model;

import org.playground.pipe.utils.SessionId;

public class TextMessage implements Message {
    private final String content;
    private SessionId sender;
    private final SessionId target;

    public TextMessage(String content, SessionId sender, SessionId target) {
        this.content = content;
        this.sender = sender;
        this.target = target;
    }

    @Override
    public String getAction() {
        return "TEXT";
    }

    @Override
    public SessionId getSender() {
        return sender;
    }

    @Override
    public SessionId getTarget() {
        return target;
    }

    @Override
    public void setSender(SessionId sender) {
        this.sender = sender;
    }

    @Override
    public String getContent() {
        return content;
    }
}
