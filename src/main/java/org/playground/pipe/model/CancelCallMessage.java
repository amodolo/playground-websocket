package org.playground.pipe.model;

import org.playground.pipe.utils.Pipe;

public class CancelCallMessage extends Message<Void> {

    @SuppressWarnings("unused")
    public CancelCallMessage(Void content, Pipe sender, Pipe target) {
        super(content, sender, target);
    }

    @SuppressWarnings("unused")
    public CancelCallMessage() {
        super();
    }

    @Override
    public String getAction() {
        return CANCEL_CALL;
    }
}
