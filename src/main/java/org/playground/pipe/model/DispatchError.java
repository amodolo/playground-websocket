package org.playground.pipe.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.playground.pipe.utils.Pipe;

import java.io.Serializable;

public class DispatchError implements Serializable {

    /**
     * Enum about possible error codes.
     */
    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    public enum ErrorCode {

        INVALID_MESSAGE("INVALID_MESSAGE", "Invalid message"),
        UNKNOWN_TARGET("UNKNOWN_TARGET", "Unknown target"),
        INITIALIZATION_FAILED("INITIALIZATION_FAILED", "Initialization failed"),
        NO_TARGET_AVAILABLE("NO_TARGET_AVAILABLE", "No target available");

        private final String code;
        private final String description;

        ErrorCode(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    private final ErrorCode errorCode;
    //FIXME: apparentemente inutile: lo eliminiamo?
    private final Pipe pipe;

    public DispatchError(ErrorCode errorCode, Pipe pipe) {
        this.errorCode = errorCode;
        this.pipe = pipe;
    }

    @JsonIgnore
    public Pipe getPipe() {
        return pipe;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    @Override
    public String toString() {
        return "DispatchError{" +
                "errorCode=" + errorCode +
                ", pipe=" + pipe +
                '}';
    }
}
