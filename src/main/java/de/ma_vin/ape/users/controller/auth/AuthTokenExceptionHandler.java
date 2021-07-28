package de.ma_vin.ape.users.controller.auth;

import de.ma_vin.ape.users.exceptions.AuthTokenException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class AuthTokenExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = AuthTokenException.class)
    protected ResponseEntity<Object> handleAuthException(AuthTokenException ex, WebRequest request) {
        return handleExceptionInternal(ex, ex.getMessage(), new HttpHeaders()
                , ex.getHttpStatus() == null ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.valueOf(ex.getHttpStatus())
                , request);
    }
}
