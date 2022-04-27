package org.playground.pipe.model;

import org.playground.pipe.utils.SessionId;

public class CallMessage extends Message<Void> {

    public CallMessage(Void content, SessionId sender, SessionId target) {
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
