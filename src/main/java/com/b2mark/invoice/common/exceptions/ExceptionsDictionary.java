/**
 * <h1></h1>
 *
 * @author b2mark
 * @version 1.0
 * @since 2018
 */

package com.b2mark.invoice.common.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;


@Getter
public enum ExceptionsDictionary {
    UNDEFINEDERROR(0,"THIS ERROR UNDEFINED",HttpStatus.BAD_REQUEST),
    UNMATCHARGUMENT(1000,"INPUT ARGUMENT IS NOT MATCH",HttpStatus.BAD_REQUEST),
    PARAMETERNOTFOUND(1001,"PARAMETER NOT FOUND",HttpStatus.BAD_REQUEST),
    IDISNOTUNIQUE(1002,"ID IS NOT UNIQUE",HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(2000,"User don't have permission",HttpStatus.UNAUTHORIZED);




    private final int id;
    private final String message;
    private final HttpStatus httpStatus;

    private ExceptionsDictionary(int id, String message,HttpStatus type) {
        this.id = id;
        this.message = message;
        this.httpStatus = type;
    }

    public String toString() {
        return this.message;
    }

    public static ExceptionsDictionary id(int id) {
        ExceptionsDictionary exceptionsDictionary = fromId(id);
        if (exceptionsDictionary == null) {
            throw new IllegalArgumentException("No matching constant for [" + id + "]");
        } else {
            return exceptionsDictionary;
        }
    }


    @Nullable
    public static ExceptionsDictionary fromId(int id) {
        ExceptionsDictionary[] var1 = values();
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            ExceptionsDictionary exceptionsDictionary = var1[var3];
            if (exceptionsDictionary.id == id) {
                return exceptionsDictionary;
            }
        }
        return null;
    }


    @Nullable
    public static ExceptionsDictionary fromName(String name) {
        for (ExceptionsDictionary exceptionsDictionary : values()) {
            if (exceptionsDictionary.getMessage().equalsIgnoreCase(name)) {
                return exceptionsDictionary;
            }
        }
        return null;
    }
}
