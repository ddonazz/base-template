package it.andrea.start.error.exception.job;

import java.io.Serial;

import it.andrea.start.error.exception.ApplicationException;
import it.andrea.start.error.exception.ErrorCode;

public class JobSchedulingException extends ApplicationException {

    @Serial
    private static final long serialVersionUID = 7614149978270160538L;

    public JobSchedulingException(Object arg1, Object arg2, Object... messageArguments) {
        super(ErrorCode.JOB_SCHEDULING_EXCEPTION, combineArguments(arg1, arg2, messageArguments));
    }

}
