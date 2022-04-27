package org.playground.pipe.model;

import org.playground.pipe.utils.SessionId;

public class TextMessage extends Message<String> {

    public TextMessage(String content, SessionId sender, SessionId target) {
        super(content, sender, target);
    }

    public TextMessage() {
        super();
    }

    @Override
    public String getAction() {
        return TEXT;
    }
}
