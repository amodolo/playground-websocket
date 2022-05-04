package org.playground.pipe.model;

import org.playground.pipe.utils.Pipe;

public class CallResponseMessage extends Message<Boolean> {

    @SuppressWarnings("unused")
    public CallResponseMessage(Boolean content, Pipe sender, Pipe target) {
        super(content, sender, target);
    }

    @SuppressWarnings("unused")
    public CallResponseMessage() {
        super();
    }

    @Override
    public String getAction() {
        return CALL_RESPONSE;
    }
}
