package org.playground.pipe.model;

import org.playground.pipe.utils.SessionId;

import java.io.Serializable;

public interface Message extends Serializable {
    String getAction();
    SessionId getSender();
    void setSender(SessionId sender);
    SessionId getTarget();
    String getContent();
}
