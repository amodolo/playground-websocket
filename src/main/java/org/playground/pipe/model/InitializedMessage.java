package org.playground.pipe.model;

import org.playground.pipe.utils.Pipe;

public class InitializedMessage extends Message<String> {

    public InitializedMessage(String content, Pipe senderAndTarget) {
        super(content, senderAndTarget, senderAndTarget);
    }

    @SuppressWarnings("unused")
    public InitializedMessage() {
        super();
    }

    @Override
    public String getAction() {
        return INITIALIZED;
    }
}
