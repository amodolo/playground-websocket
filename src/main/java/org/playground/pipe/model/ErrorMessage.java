package org.playground.pipe.model;

import org.playground.pipe.utils.Pipe;

public class ErrorMessage extends Message<DispatchError> {
    public ErrorMessage() {
    }

    public ErrorMessage(DispatchError content, Pipe sender, Pipe target) {
        super(content, sender, target);
    }

    @Override
    public java.lang.String getAction() {
        return ERROR;
    }
}
