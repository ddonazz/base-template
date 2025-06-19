package it.andrea.start.error.exception.job;

import java.io.Serial;

import it.andrea.start.error.exception.ApplicationException;
import it.andrea.start.error.exception.ErrorCode;

public class JobNotFoundException extends ApplicationException {

    @Serial
    private static final long serialVersionUID = -3259290432738968345L;

    public JobNotFoundException(Object arg1, Object arg2, Object... messageArguments) {
        super(ErrorCode.JOB_NOT_FOUND_EXCEPTION, combineArguments(arg1, arg2, messageArguments));
    }

}
