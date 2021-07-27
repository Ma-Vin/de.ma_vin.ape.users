package de.ma_vin.ape.users.exceptions;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class AuthTokenException extends RuntimeException {

    private final Integer httpStatus;

    public AuthTokenException(String message) {
        this(message, (Integer) null);
    }

    public AuthTokenException(String message, Throwable cause) {
        this(message, cause, null);
    }

    public AuthTokenException(Throwable cause) {
        this(cause, null);
    }

    public AuthTokenException(String message, Integer httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public AuthTokenException(String message, Throwable cause, Integer httpStatus) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }

    public AuthTokenException(Throwable cause, Integer httpStatus) {
        super(cause);
        this.httpStatus = httpStatus;
    }
}
