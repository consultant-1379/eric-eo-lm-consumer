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
package com.ericsson.licenseconsumer.datalayer.service;

import com.ericsson.licenseconsumer.datalayer.entity.LicenseKey;
import com.ericsson.licenseconsumer.datalayer.model.ApplicationType;
import com.ericsson.licenseconsumer.datalayer.model.LicenseToLKFMapping;

public interface LicenseDbService {

    LicenseKey saveLicense(ApplicationType application, LicenseToLKFMapping license);
    LicenseKey findByApplication(String application);
    LicenseToLKFMapping findByApplicationAndMap(String application);
    void deleteLicenseKey(LicenseKey licenseKey);
    void deleteAll();
}
