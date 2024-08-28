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
package com.ericsson.licenseconsumer.lm.integration.util;

import com.ericsson.licenseconsumer.lm.integration.model.LicenseData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class JsonUtilTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final JsonUtil jsonUtil = new JsonUtil(objectMapper);

    @Test
    void shouldSerializeAndDeserializeJson() throws JsonProcessingException {
        String json = getJson();

        LicenseData licenseData = jsonUtil.deserializeJson(json, LicenseData.class);

        assertNotNull(licenseData);
        assertNotNull(licenseData.getOperationalStatusInfo());

        String licenseDataJson = jsonUtil.serializeJson(licenseData);

        assertEquals(licenseDataJson, json);
    }

    private String getJson() {
        return "{\"licenseRequestPeriod\":2,\"licensesInfo\":[{\"license\":{\"keyId\":\"keyId\",\"type\":\"type\"},\"licenseStatus\":\"VALID\"}],"
                + "\"operationalStatusInfo\":{\"operationalMode\":\"operationalMode\",\"autonomousModeDuration\":5},"
                + "\"usageReportPeriodCumulative\":3,\"usageReportPeriodPeak\":1}";
    }
}
