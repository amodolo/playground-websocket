package org.playground.pipe.model;

import org.playground.pipe.utils.Pipe;

public class CallMessage extends Message<Void> {

    public CallMessage(Void content, Pipe sender, Pipe target) {
        super(content, sender, target);
    }

    public CallMessage() {
        super();
    }

    @Override
    public String getAction() {
        return CALL;
    }
}
