package org.playground.pipe.model;

import org.playground.pipe.utils.SessionId;

public abstract class ReplyMessage<T> extends Message<T> {
    private final Message<?> original;

    public ReplyMessage(T content, SessionId sender, SessionId target, Message<?> original) {
        super(content, sender, target);
        this.original = original;
    }

    public Message<?> getOriginal() {
        return original;
    }
}
