package com.b2mark.invoice.common.exceptions;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
public class ExceptionResponse {
    private Date date;
    private int code;
    private String message;
    private String description;

    public ExceptionResponse(ExceptionsDictionary exceptionsDictionary) {
        code = exceptionsDictionary.getId();
        message = exceptionsDictionary.getMessage();
        date = new Date();
    }

    public ExceptionResponse(ExceptionsDictionary exceptionsDictionary, String description) {
        code = exceptionsDictionary.getId();
        message = exceptionsDictionary.getMessage();
        date = new Date();
        this.description = description;
    }

}