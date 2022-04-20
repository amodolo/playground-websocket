package org.playground.pipe.model;

public abstract class ReplyMessage implements Message {
    private final Message original;

    public ReplyMessage(Message original) {
        this.original = original;
    }

    public Message getOriginal() {
        return original;
    }

    @Override
    public String getType() {
        return Message.REPLY;
    }
}
