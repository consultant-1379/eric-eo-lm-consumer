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

import lombok.Getter;

@Getter
public enum LicenseToLKFMapping {

    CVNFM_FULL_KEY("FAT1024423", ApplicationType.CVNFM, 1),
    CVNFM_GEORED_KEY("FAT1024423/5", ApplicationType.CVNFM, 2),
    CVNFM_LIMITED_KEY("FAT1024423/3", ApplicationType.CVNFM, 3),

    VM_VNFM_FULL_KEY("FAT1024423/2", ApplicationType.VMVNFM, 1),
    VM_VNFM_GEORED_KEY("FAT1024423/6", ApplicationType.VMVNFM, 2),
    VM_VNFM_LIMITED_KEY("FAT1024423/4", ApplicationType.VMVNFM, 3);

    private final String licenseKeyId;
    private final ApplicationType applicationType;
    private final Integer priority;

    LicenseToLKFMapping(final String licenseKeyId, final ApplicationType applicationType, final Integer priority) {
        this.licenseKeyId = licenseKeyId;
        this.applicationType = applicationType;
        this.priority = priority;
    }

    public static LicenseToLKFMapping fromString(final String licenseId) {
        for (LicenseToLKFMapping license : LicenseToLKFMapping.values()) {
            if (license.licenseKeyId.equalsIgnoreCase(licenseId)) {
                return license;
            }
        }
        return null;
    }
}
