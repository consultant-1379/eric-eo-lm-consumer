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
package com.ericsson.licenseconsumer.job;

import java.util.Set;

import com.ericsson.licenseconsumer.datalayer.model.LicenseToLKFMapping;

public interface PollLicensesJobService {
    Set<LicenseToLKFMapping> processAllLicenses();

    LicenseToLKFMapping processLicenseForApplication(String application);
}
