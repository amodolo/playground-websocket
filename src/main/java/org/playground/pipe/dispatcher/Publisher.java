package org.playground.pipe.dispatcher;

import org.playground.pipe.model.DispatchError;
import org.playground.pipe.model.Message;

public interface Publisher {
    DispatchError send(Message message);
}
