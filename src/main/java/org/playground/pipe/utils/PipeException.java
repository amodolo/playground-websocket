package org.playground.pipe.utils;

public class PipeException extends Exception {
    public PipeException(String message) {
        super(message);
    }

    public PipeException(String message, Throwable cause) {
        super(message, cause);
    }

    public PipeException(Throwable cause) {
        super(cause);
    }
}
