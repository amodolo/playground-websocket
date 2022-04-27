package org.playground.pipe.model;

import org.playground.pipe.utils.Pipe;

public abstract class ReplyMessage<S, T> extends Message<S> {
    private final Message<T> original;

    protected ReplyMessage(S content, Pipe sender, Message<T> original) {
        super(content, sender, original.getSender());
        this.original = original;
    }

    public Message<T> getOriginal() {
        return original;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ReplyMessage<?, ?> that = (ReplyMessage<?, ?>) o;

        return original.equals(that.original);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + original.hashCode();
        return result;
    }
}
