package org.playground.pipe.model;

import org.playground.pipe.utils.Pipe;

import java.io.Serializable;

public class DispatchError implements Serializable {
    private final Pipe pipe;
    private final String description;
    private final Exception exception;

    //FIXME: forse meglio accodare il messaggio dell'exception alla description? Questo costruttore viene usato solo in caso di EncodeException.... (vedi org.playground.pipe.dispatcher.redis.RedisPublisher.write)
    public DispatchError(Pipe pipe, String description, Exception exception) {
        this.pipe = pipe;
        this.description = description;
        this.exception = exception;
    }

    public DispatchError(Pipe pipe, String description) {
        this(pipe, description, null);
    }

    public Pipe getPipe() {
        return pipe;
    }

    public String getDescription() {
        return description;
    }

    public Exception getException() {
        return exception;
    }

    @Override
    public String toString() {
        return "DispatchError{" +
                "pipe=" + pipe +
                ", description='" + description + '\'' +
                ", exception=" + exception +
                '}';
    }
}
