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
package com.ericsson.licenseconsumer.datalayer.entity;

import java.io.Serializable;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Generated;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Generated
public class ApplicationLicenseKeyId implements Serializable {

    private static final long serialVersionUID = 0;

    private String licenseKeyId;
    private String application;

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ApplicationLicenseKeyId that = (ApplicationLicenseKeyId) o;
        return Objects.equals(licenseKeyId, that.licenseKeyId) && Objects.equals(application, that.application);
    }

    @Override
    public int hashCode() {
        return Objects.hash(licenseKeyId, application);
    }
}