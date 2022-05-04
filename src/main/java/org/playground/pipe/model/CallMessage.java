package org.playground.pipe.model;

import org.playground.pipe.utils.Pipe;

public class CallMessage extends Message<Void> {

    @SuppressWarnings("unused")
    public CallMessage(Void content, Pipe sender, Pipe target) {
        super(content, sender, target);
    }

    @SuppressWarnings("unused")
    public CallMessage() {
        super();
    }

    @Override
    public String getAction() {
        return CALL;
    }
}
