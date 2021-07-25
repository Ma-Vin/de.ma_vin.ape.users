package de.ma_vin.ape.users.exceptions;

public class CryptException extends Exception {
    public CryptException(String message) {
        super(message);
    }

    public CryptException(String message, Throwable cause) {
        super(message, cause);
    }

    public CryptException(Throwable cause) {
        super(cause);
    }
}
