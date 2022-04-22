package org.playground.pipe.model;

import org.playground.pipe.utils.SessionId;

public class CancelCallMessage extends Message<Void> {

    public CancelCallMessage(Void content, SessionId sender, SessionId target) {
        super(content, sender, target);
    }

    @Override
    public String getAction() {
        return CANCEL_CALL;
    }
}
