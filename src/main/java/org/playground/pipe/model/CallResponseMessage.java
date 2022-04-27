package org.playground.pipe.model;

import org.playground.pipe.utils.SessionId;

public class CallResponseMessage extends Message<Boolean> {
    public CallResponseMessage(Boolean content, SessionId sender, SessionId target) {
        super(content, sender, target);
    }

    public CallResponseMessage() {
        super();
    }

    @Override
    public String getAction() {
        return CALL_RESPONSE;
    }
}
