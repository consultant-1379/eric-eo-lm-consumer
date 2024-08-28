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
package com.ericsson.licenseconsumer.lm.integration;

import java.io.IOException;
import java.net.URISyntaxException;

import com.ericsson.licenseconsumer.lm.integration.model.LicenseData;
import com.ericsson.licenseconsumer.lm.integration.model.request.ProductTypeLicenseRequest;

public interface LicenseConsumerApi {

    LicenseData getLicenses(ProductTypeLicenseRequest productTypeRequest) throws IOException, URISyntaxException;

    LicenseData ensureLicenses(ProductTypeLicenseRequest productTypeLicenseRequest) throws IOException, URISyntaxException;
}
