package com.b2mark.invoice.exception;

import com.b2mark.common.exceptions.ExceptionResponse;
import com.b2mark.common.exceptions.ExceptionsDictionary;
import com.b2mark.common.exceptions.PublicException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;


@ControllerAdvice("com.b2mark.invoice")
@RestController
public class AdviceExceptionHandler extends ResponseEntityExceptionHandler {


    @ExceptionHandler(Exception.class)
    public final ResponseEntity<ExceptionResponse> handleAllExceptions(Exception ex, WebRequest request) {
        ExceptionsDictionary exceptionsDictionary = ExceptionsDictionary.UNDEFINEDERROR;
        ExceptionResponse exceptionResponse = new ExceptionResponse(exceptionsDictionary);
        return new ResponseEntity<>(exceptionResponse, exceptionsDictionary.getHttpStatus());
    }


    @ExceptionHandler(PublicException.class)
    public final ResponseEntity<ExceptionResponse> parameterNotFoundException(Exception ex, WebRequest request) {
        PublicException ex1 = (PublicException) ex;
        ExceptionResponse exceptionResponse = new ExceptionResponse(ex1.getExceptionsDictionary(), ex.getMessage());
        return new ResponseEntity<>(exceptionResponse, ex1.getExceptionsDictionary().getHttpStatus());
    }


    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public final ResponseEntity<ExceptionResponse> handleArgumentFailed(Exception ex, WebRequest request) {
        ExceptionsDictionary exceptionsDictionary = ExceptionsDictionary.UNMATCHARGUMENT;
        ExceptionResponse exceptionResponse = new ExceptionResponse(exceptionsDictionary);
        return new ResponseEntity<>(exceptionResponse, exceptionsDictionary.getHttpStatus());
    }

    @ExceptionHandler(NumberFormatException.class)
    public final ResponseEntity<ExceptionResponse> handleNumberFailed(Exception ex, WebRequest request) {
        ExceptionsDictionary exceptionsDictionary = ExceptionsDictionary.NUMBERISNOTVALID;
        ExceptionResponse exceptionResponse = new ExceptionResponse(exceptionsDictionary);
        return new ResponseEntity<>(exceptionResponse, exceptionsDictionary.getHttpStatus());
    }
}
