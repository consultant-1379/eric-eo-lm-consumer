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
package contracts.GetPermissions

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return cvnfm permissions"
    request{
        method GET()
        url("/lc/v1/cvnfm/permissions") {
        }
    }
    response {
        body("[\"onboarding\",\"enm_integration\",\"lcm_operations\",\"cluster_management\"]")
        status 200
        headers {
            contentType(applicationJson())
        }
    }
}
