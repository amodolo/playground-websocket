package org.playground.pipe.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.util.Objects;

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

        @SuppressWarnings("unused")
        public String getDescription() {
            return description;
        }
    }

    private final ErrorCode errorCode;

    public DispatchError(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DispatchError that = (DispatchError) o;
        return errorCode == that.errorCode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(errorCode);
    }

    @Override
    public String toString() {
        return "DispatchError{" +
                "errorCode=" + errorCode +
                '}';
    }
}
