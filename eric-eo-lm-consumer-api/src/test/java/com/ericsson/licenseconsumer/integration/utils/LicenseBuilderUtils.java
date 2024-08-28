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
package com.ericsson.licenseconsumer.integration.utils;

import static com.ericsson.licenseconsumer.lm.integration.model.LicenseStatus.VALID;

import com.ericsson.licenseconsumer.datalayer.model.LicenseToLKFMapping;
import com.ericsson.licenseconsumer.lm.integration.model.LicenseData;
import com.ericsson.licenseconsumer.lm.integration.model.LicenseInfo;
import com.ericsson.licenseconsumer.lm.integration.model.LicenseModel;

import java.util.List;

public final class LicenseBuilderUtils {

    private LicenseBuilderUtils() {
    }

    public static LicenseData buildLMResponse(LicenseToLKFMapping license) {
        LicenseData licenseData = new LicenseData();
        LicenseInfo licenseInfo = new LicenseInfo();
        licenseInfo.setLicenseStatus(VALID.toString());

        LicenseModel licenseModel =
                new LicenseModel(license.getLicenseKeyId(), license.getApplicationType().getName());

        licenseInfo.setLicense(licenseModel);
        licenseData.setLicensesInfo(List.of(licenseInfo));

        return licenseData;
    }

    public static LicenseData buildEmptyLicenseData() {
        return LicenseData.builder().licensesInfo(List.of()).build();
    }

}
