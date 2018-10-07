package com.b2mark.invoice.exception;

import com.b2mark.invoice.common.exceptions.ExceptionResponse;
import com.b2mark.invoice.common.exceptions.ExceptionsDictionary;
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


    @ExceptionHandler(BadRequest.class)
    public final ResponseEntity<ExceptionResponse> badRequestException(Exception ex, WebRequest request) {
        ExceptionsDictionary exceptionsDictionary = ExceptionsDictionary.UNDEFINEDERROR;
        ExceptionResponse exceptionResponse = new ExceptionResponse(exceptionsDictionary,ex.getMessage());
        return new ResponseEntity<>(exceptionResponse, exceptionsDictionary.getHttpStatus());
    }



    @ExceptionHandler(ParameterNotFound.class)
    public final ResponseEntity<ExceptionResponse> parameterNotFoundException(Exception ex, WebRequest request) {
        ExceptionsDictionary exceptionsDictionary = ExceptionsDictionary.PARAMETERNOTFOUND;
        ExceptionResponse exceptionResponse = new ExceptionResponse(exceptionsDictionary,ex.getMessage());
        return new ResponseEntity<>(exceptionResponse, exceptionsDictionary.getHttpStatus());
    }




    @ExceptionHandler(IdNotUnique.class)
    public final ResponseEntity<ExceptionResponse> idNotUniqueException(Exception ex, WebRequest request) {
        ExceptionsDictionary exceptionsDictionary = ExceptionsDictionary.IDISNOTUNIQUE;
        ExceptionResponse exceptionResponse = new ExceptionResponse(exceptionsDictionary,ex.getMessage());
        return new ResponseEntity<>(exceptionResponse, exceptionsDictionary.getHttpStatus());
    }

    @ExceptionHandler(Unauthorized.class)
    public final ResponseEntity<ExceptionResponse> unAuthorizedException(Exception ex, WebRequest request) {
        ExceptionsDictionary exceptionsDictionary = ExceptionsDictionary.UNAUTHORIZED;
        ExceptionResponse exceptionResponse = new ExceptionResponse(exceptionsDictionary,ex.getMessage());
        return new ResponseEntity<>(exceptionResponse, exceptionsDictionary.getHttpStatus());
    }



    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public final ResponseEntity<ExceptionResponse> handleArgumentFailed(Exception ex, WebRequest request) {
        ExceptionsDictionary exceptionsDictionary = ExceptionsDictionary.UNMATCHARGUMENT;
        ExceptionResponse exceptionResponse = new ExceptionResponse(exceptionsDictionary);
        return new ResponseEntity<>(exceptionResponse, exceptionsDictionary.getHttpStatus());
    }


}
