
/**
 * <h1>Exception when bad call api. parameter mistake or anythings</h1>
 * <b>HTTP CODE:</b>204 no_content
 *
 * @author b2mark
 * @version 1.0
 * @since 2018
 */

package com.b2mark.invoice.exception;


import com.b2mark.invoice.common.exceptions.ExceptionsDictionary;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class PublicException extends RuntimeException {

    @Getter
    ExceptionsDictionary exceptionsDictionary = ExceptionsDictionary.UNDEFINEDERROR;

    public PublicException(ExceptionsDictionary exceptionsDictionary, String description) {
        super(description);
        this.exceptionsDictionary = exceptionsDictionary;
    }
}