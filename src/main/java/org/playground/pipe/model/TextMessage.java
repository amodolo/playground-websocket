package org.playground.pipe.model;

import org.playground.pipe.utils.SessionId;

import java.util.Objects;

public class TextMessage implements Message {
    private String content;
    private SessionId sender;
    private SessionId target;

    @SuppressWarnings("unused")
    public TextMessage() {}

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
    public String getContent() {
        return content;
    }
    @Override
    public String getType() {
        return Message.MSG;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TextMessage message = (TextMessage) o;
        return content.equals(message.content) && sender.equals(message.sender) && target.equals(message.target) && getType().equals(message.getType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, sender, target, getType());
    }

    @Override
    public String toString() {
        return "TextMessage{" +
                "content='" + content + '\'' +
                ", sender=" + sender +
                ", target=" + target +
                ", type=" + getType() +
                '}';
    }
}
