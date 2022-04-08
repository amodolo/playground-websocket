package org.playground.pipe.dispatcher;

import org.playground.pipe.model.DispatchError;
import org.playground.pipe.model.Message;

import java.util.Set;

public interface Publisher {
    Set<DispatchError> send(Message message);
}
