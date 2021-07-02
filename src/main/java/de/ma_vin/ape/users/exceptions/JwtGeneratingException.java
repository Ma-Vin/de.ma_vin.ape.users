package de.ma_vin.ape.users.exceptions;

public class JwtGeneratingException extends Exception {
    public JwtGeneratingException(String message) {
        super(message);
    }

    public JwtGeneratingException(String message, Throwable cause) {
        super(message, cause);
    }

    public JwtGeneratingException(Throwable cause) {
        super(cause);
    }
}
