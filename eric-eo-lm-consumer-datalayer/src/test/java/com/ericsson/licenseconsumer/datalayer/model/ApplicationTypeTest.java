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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.ericsson.licenseconsumer.datalayer.exception.ApplicationNotFoundException;

import org.junit.jupiter.api.Test;

class ApplicationTypeTest {

    @Test
    void shouldGetApplicationTypeByName() {
        assertEquals(ApplicationType.fromString("cvnfm").getName(), ApplicationType.CVNFM.getName());
        assertEquals(ApplicationType.fromString("vmvnfm").getName(), ApplicationType.VMVNFM.getName());
    }

    @Test
    void shoudThrowApplicationNotFoundException() {
        assertThrows(ApplicationNotFoundException.class, () -> ApplicationType.fromString("Non-existent name"));
        assertThrows(ApplicationNotFoundException.class, () -> ApplicationType.fromString(null));
    }

    @Test
    void shouldReturnNullForNonHardcodedLicenses() {
        assertNull(LicenseToLKFMapping.fromString("Non-existent name"));
        assertNull(LicenseToLKFMapping.fromString(null));
    }
}
