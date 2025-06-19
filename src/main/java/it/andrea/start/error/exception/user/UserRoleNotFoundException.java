package it.andrea.start.error.exception.user;

import java.io.Serial;

import it.andrea.start.error.exception.ApplicationException;
import it.andrea.start.error.exception.ErrorCode;

public class UserRoleNotFoundException extends ApplicationException {

    @Serial
    private static final long serialVersionUID = -2104139028316052461L;

    public UserRoleNotFoundException(Object arg1, Object... messageArguments) {
        super(ErrorCode.USER_ROLE_NOT_FOUND, combineArguments(arg1, messageArguments));
    }

}
