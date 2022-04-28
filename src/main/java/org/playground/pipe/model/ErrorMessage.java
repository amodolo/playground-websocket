package org.playground.pipe.model;

import org.playground.pipe.utils.Pipe;

public class ErrorMessage extends Message<DispatchError.ErrorCode> {
    @SuppressWarnings("unused")
    public ErrorMessage() {
    }

    public ErrorMessage(DispatchError.ErrorCode content, Pipe senderAndTarget) {
        super(content, senderAndTarget, senderAndTarget);
    }

    @Override
    public java.lang.String getAction() {
        return ERROR;
    }
}
