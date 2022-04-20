package org.playground.pipe.dispatcher;

import javax.websocket.Session;
import java.util.function.BiFunction;

/**
 * Interface to read a message from a message broker and dispatch it to the recipient.
 */
public interface MessageConsumer extends BiFunction<String, Session, Boolean> {

    /**
     * Reads a message, intended to a recipient identified by the provided {@code recipientKey}, from a message broker and dispatch it to the recipient.<br/>
     * Returns {@code true} in case of successful result; {@code false} otherwise.
     *
     * @param recipientKey     {@link String} about the recipient key.
     * @param recipientSession {@link Session} about the recipient session to dispatch the message to.
     * @return {@code true} in case of successful result; {@code false} otherwise.
     */
    Boolean apply(String recipientKey, Session recipientSession);
}
