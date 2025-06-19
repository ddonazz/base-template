package it.andrea.start.error.exception;

import java.io.Serial;

public abstract class ApplicationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -8236888816462673842L;

    private final ErrorCode errorCode;
    private final transient Object[] messageArguments;

    protected ApplicationException(ErrorCode errorCode, Object... messageArguments) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
        this.messageArguments = messageArguments;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Object[] getMessageArguments() {
        return messageArguments;
    }

    protected static Object[] combineArguments(Object arg1, Object... messageArguments) {
        Object[] combined = new Object[2 + messageArguments.length];
        combined[0] = arg1;
        System.arraycopy(messageArguments, 0, combined, 1, messageArguments.length);
        return combined;
    }

    protected static Object[] combineArguments(Object arg1, Object arg2, Object... messageArguments) {
        Object[] combined = new Object[2 + messageArguments.length];
        combined[0] = arg1;
        combined[1] = arg2;
        System.arraycopy(messageArguments, 0, combined, 2, messageArguments.length);
        return combined;
    }
}