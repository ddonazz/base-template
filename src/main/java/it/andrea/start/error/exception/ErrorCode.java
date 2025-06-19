package it.andrea.start.error.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum ErrorCode {

 // --- Internal Generic Errors ---
    ERROR_INTERNAL_SERVER_ERROR("error.internal.server.error", "An internal server error occurred.", HttpStatus.INTERNAL_SERVER_ERROR, null),
    ERROR_TRANSACTION_ROLLED_BACK("error.transaction.rollback", "The transaction was rolled back.", HttpStatus.INTERNAL_SERVER_ERROR, null),
    ERROR_PERSISTENCE_EXCEPTION("error.persistence.exception", "A database persistence error occurred.", HttpStatus.INTERNAL_SERVER_ERROR, null),
    ERROR_CONSTRAINT_VIOLATION("error.constraint.violation", "A data integrity constraint was violated.", HttpStatus.CONFLICT, null),
    ERROR_SQL_GENERIC("error.sql.generic", "A generic SQL error occurred.", HttpStatus.INTERNAL_SERVER_ERROR, null),
    ERROR_NULL_POINTER("error.null.pointer", "A null pointer exception occurred.", HttpStatus.INTERNAL_SERVER_ERROR, null),

    // --- Authorization Login Errors ---
    USER_AUTHORIZE_LOGIN_ACCOUNT_PENDING("user.authorize.login.account.pending", "User account is pending activation.", HttpStatus.UNAUTHORIZED, "User"),
    USER_AUTHORIZE_LOGIN_ACCOUNT_SUSPENDED("user.authorize.login.account.suspended", "User account is suspended.", HttpStatus.UNAUTHORIZED, "User"),
    USER_AUTHORIZE_LOGIN_ACCOUNT_DEACTIVE("user.authorize.login.account.deactivate", "User account is deactivated.", HttpStatus.UNAUTHORIZED, "User"),
    USER_AUTHORIZE_LOGIN_ACCOUNT_BLACKLIST("user.authorize.login.account.blacklist", "User account is blacklisted.", HttpStatus.UNAUTHORIZED, "User"),

    // --- Authorization User/Password Errors (Logica di Auth) ---
    AUTHENTICATION_FAILED("authentication.failed", "Authentication failed. Invalid credentials.", HttpStatus.UNAUTHORIZED, "User"),
    AUTHORIZEUSER_USERNAME_NULL("authorize.user.username.null", "Username cannot be null.", HttpStatus.BAD_REQUEST, "User"),
    AUTHORIZEUSER_PASSWORD_NULL("authorize.user.password.null", "Password cannot be null.", HttpStatus.BAD_REQUEST, "User"),
    AUTHORIZEUSER_USER_NOT_FOUND("authorize.user.user.not.found", "User not found.", HttpStatus.UNAUTHORIZED, "User"),
    AUTHORIZEUSER_PASSWORD_WRONG("authorize.user.password.wrong", "Incorrect password.", HttpStatus.UNAUTHORIZED, "User"),

    // --- User Management Errors (Logica Applicativa) ---
    USER_ID_NULL("error.user.id.null", "User ID cannot be null.", HttpStatus.BAD_REQUEST, "User"),
    USER_NOT_FOUND("error.user.id.not.found", "User {0} not found.", HttpStatus.NOT_FOUND, "User"),
    USER_NOT_ACTIVE("error.user.id.not.active", "User account is not active.", HttpStatus.FORBIDDEN, "User"),
    USER_ALREADY_EXISTS("error.user.id.already.exists", "User with {0} ID already exists.", HttpStatus.CONFLICT, "User"),
    USER_USERNAME_ALREADY_USED("error.user.username.already.used", "Username is already in use.", HttpStatus.CONFLICT, "User"),
    USER_EMAIL_ALREADY_USED("error.user.email.already.used", "Email is already in use.", HttpStatus.CONFLICT, "User"),
    USER_ROLE_NULL("error.user.role.null", "User role cannot be null.", HttpStatus.BAD_REQUEST, "User"),

    // --- User Role Business Logic Errors ---
    USER_ROLE_NOT_FOUND("error.user.role.not.found", "User role {0} not found.", HttpStatus.NOT_FOUND, "User"),
    USER_ROLE_ADMIN_NOT_USABLE("error.user.role.admin.not.usable", "Admin role cannot be used for this operation.", HttpStatus.BAD_REQUEST, "User"),
    USER_ROLE_MANAGER_NOT_USABLE("error.user.role.manager.not.usable", "Manager role cannot be used for this operation.", HttpStatus.BAD_REQUEST, "User"),
    USER_ROLE_ADMIN_NOT_DELETE("error.user.role.admin.not.delete", "Admin user cannot be deleted.", HttpStatus.FORBIDDEN, "User"),
    USER_ROLE_MANAGER_NOT_DELETE("error.user.role.manager.not.delete", "Manager user cannot be deleted.", HttpStatus.FORBIDDEN, "User"),
    USER_ROLE_ADMIN_NOT_CHANGE_PASSWORD("error.user.role.admin.not.change.password", "Admin user password cannot be changed this way.", HttpStatus.FORBIDDEN, "User"),
    USER_ROLE_MANAGER_NOT_CHANGE_PASSWORD("error.user.role.manager.not.change.password", "Manager user password cannot be changed this way.", HttpStatus.FORBIDDEN, "User"),
    USER_REPEAT_PASSWORD_NOT_EQUAL("error.user.repeat.password.not.equal", "The repeated password does not match.", HttpStatus.BAD_REQUEST, "User"),

    // --- Job Logic Errors ---
    JOB_NOT_FOUND_EXCEPTION("job.not.found.exception", "Job {0}//{1} not found.", HttpStatus.NOT_FOUND, "Job"),
    JOB_SCHEDULING_EXCEPTION("job.scheduling.exception", "An error occurred during job {0}//{1} scheduling.", HttpStatus.INTERNAL_SERVER_ERROR, "Job"),
    JOB_CONTROL_EXCEPTION("job.control.exception", "An error occurred during job control.", HttpStatus.INTERNAL_SERVER_ERROR, "Job");

    private final String code;
    private final String defaultMessage; 
    private final HttpStatus httpStatus;
    private final String entity;

    ErrorCode(String code, String defaultMessage, HttpStatus httpStatus, String entity) { 
        this.code = code;
        this.defaultMessage = defaultMessage; 
        this.httpStatus = httpStatus;
        this.entity = entity;
    }

    public static ErrorCode getDefault() {
        return ERROR_INTERNAL_SERVER_ERROR;
    }

}