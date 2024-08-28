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
package com.ericsson.licenseconsumer.datalayer.model;

import com.ericsson.licenseconsumer.datalayer.exception.ApplicationNotFoundException;

import lombok.Getter;

@Getter
public enum ApplicationType {
    CVNFM("cvnfm"),
    VMVNFM("vmvnfm");

    private final String name;

    ApplicationType(final String name) {
        this.name = name;
    }

    public static ApplicationType fromString(String text) {
        for (ApplicationType b : ApplicationType.values()) {
            if (b.name.equalsIgnoreCase(text)) {
                return b;
            }
        }
        throw new ApplicationNotFoundException(text);
    }

}
