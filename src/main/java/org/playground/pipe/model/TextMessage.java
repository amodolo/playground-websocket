package org.playground.pipe.model;

import org.playground.pipe.utils.Pipe;

public class TextMessage extends Message<String> {

    public TextMessage(String content, Pipe sender, Pipe target) {
        super(content, sender, target);
    }

    @SuppressWarnings("unused")
    public TextMessage() {
        super();
    }

    @Override
    public String getAction() {
        return TEXT;
    }
}
