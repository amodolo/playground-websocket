package org.playground.pipe.model;

import org.playground.pipe.utils.SessionId;

import java.io.Serializable;

public class DispatchError implements Serializable {
    private final SessionId id;
    private final String description;
    private final Exception exception;

    //FIXME: forse meglio accodare il messaggio dell'exception alla description? Questo costruttore viene usato solo in caso di EncodeException.... (vedi org.playground.pipe.dispatcher.redis.RedisPublisher.write)
    public DispatchError(SessionId id, String description, Exception exception) {
        this.id = id;
        this.description = description;
        this.exception = exception;
    }

    public DispatchError(SessionId id, String description) {
        this(id, description, null);
    }

    public SessionId getId() {
        return id;
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
                "id=" + id +
                ", description='" + description + '\'' +
                '}';
    }
}
