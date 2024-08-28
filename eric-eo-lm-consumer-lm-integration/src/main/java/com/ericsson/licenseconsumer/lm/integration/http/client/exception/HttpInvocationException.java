/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 *
 *
 * The copyright to the computer program(s) herein is the property of
 *
 * Ericsson Inc. The programs may be used and/or copied only with written
 *
 * permission from Ericsson Inc. or in accordance with the terms and
 *
 * conditions stipulated in the agreement/contract under which the
 *
 * program(s) have been supplied.
 ******************************************************************************/
package com.ericsson.licenseconsumer.lm.integration.http.client.exception;

public class HttpInvocationException extends RuntimeException {

    public HttpInvocationException() {
        super();
    }

    public HttpInvocationException(final String message) {
        super(message);
    }

    public HttpInvocationException(Throwable throwable) {
        super(throwable);
    }
}
