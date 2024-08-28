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
package com.ericsson.licenseconsumer.mapping.model;

public enum Permission {
    ONBOARDING("onboarding"),
    ENM_INTEGRATION("enm_integration"),
    LCM_OPERATIONS("lcm_operations"),
    CLUSTER_MANAGEMENT("cluster_management");

    private final String name;

    Permission(final String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
