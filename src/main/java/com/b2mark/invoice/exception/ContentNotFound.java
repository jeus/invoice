/**
 * <h1>Exception when data not found</h1>
 * <p> This excption when call that information not found in database.
 * <b>HTTP CODE:</b>204 no_content
 *
 * @author b2mark
 * @version 1.0
 * @since 2018
 */

package com.b2mark.invoice.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NO_CONTENT)
public class ContentNotFound extends RuntimeException {

    public ContentNotFound() {
        super();
    }

    public ContentNotFound(String message, Throwable cause) {
        super(message, cause);
    }

    public ContentNotFound(String message) {
        super(message);
    }

    public ContentNotFound(Throwable cause) {
        super(cause);
    }

}

