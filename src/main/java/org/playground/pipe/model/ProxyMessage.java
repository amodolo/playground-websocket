package org.playground.pipe.model;

import org.playground.pipe.utils.Pipe;

public class ProxyMessage<T> extends Message<T> {

    private final Message<T> message;


    public ProxyMessage(Message<T> message, Pipe sender) {
        super(message.getContent(), sender, message.getTarget());
        this.message = message;
    }

    @Override
    public String getAction() {
        return message.getAction();
    }

    @Override
    public Pipe getTarget() {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ProxyMessage<?> that = (ProxyMessage<?>) o;

        return message.equals(that.message);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + message.hashCode();
        return result;
    }
}
