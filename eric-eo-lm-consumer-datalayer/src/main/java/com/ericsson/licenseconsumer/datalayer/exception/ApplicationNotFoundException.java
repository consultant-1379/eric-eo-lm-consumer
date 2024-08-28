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
package com.ericsson.licenseconsumer.datalayer.exception;

import com.ericsson.licenseconsumer.datalayer.model.ApplicationType;
import lombok.Generated;

@Generated
public class ApplicationNotFoundException extends RuntimeException {
    public ApplicationNotFoundException(final String app) {
        super(String.format("No license found by application name '%s'.", app));
    }

    public ApplicationNotFoundException(final ApplicationType app) {
        super(String.format("No license found by application name '%s'.", app.getName()));
    }

}
