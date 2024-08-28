#
# COPYRIGHT Ericsson 2024
#
#
#
# The copyright to the computer program(s) herein is the property of
#
# Ericsson Inc. The programs may be used and/or copied only with written
#
# permission from Ericsson Inc. or in accordance with the terms and
#
# conditions stipulated in the agreement/contract under which the
#
# program(s) have been supplied.
#

ARG BASE_IMAGE_VERSION
FROM armdocker.rnd.ericsson.se/proj-am/sles/sles-corretto-openjdk17:${BASE_IMAGE_VERSION}

ARG ACTIVE_PROFILE=''
ENV ENV_ACTIVE_PROFILE ${ACTIVE_PROFILE}

ARG GIT_COMMIT=""
ARG APP_VERSION=""
ARG BUILD_TIME=""

LABEL product.number="CXU 101 1810" \
      product.revision="R1A" \
      GIT_COMMIT=$GIT_COMMIT \
      com.ericsson.product-name="EO License Consumer" \
      com.ericsson.product-number="CXU 101 1810" \
      com.ericsson.product-revision="R1A" \
      org.opencontainers.image.title="EO License Consumer" \
      org.opencontainers.image.created=${BUILD_TIME} \
      org.opencontainers.image.revision=${GIT_COMMIT} \
      org.opencontainers.image.version=${APP_VERSION} \
      org.opencontainers.image.vendor="Ericsson"

# User Id generated based on ADP rule DR-D1123-122 (eric-eo-lm-consumer : 186080)
ARG uid=186080
ARG gid=186080

RUN mkdir -p /home/appuser && chown ${uid}:${gid} /home/appuser
WORKDIR /home/appuser

RUN echo "${uid}:x:${uid}:${gid}:eric-eo-lm-consumer-user:/:/bin/bash" >> /etc/passwd
RUN sed -i '/root/s/bash/false/g' /etc/passwd

ADD eric-eo-lm-consumer-api/target/eric-eo-lm-consumer.jar eric-eo-lm-consumer.jar

COPY entryPoint.sh entryPoint.sh

RUN chmod 755 entryPoint.sh

RUN chown ${uid}:${gid} /var/lib/ca-certificates/java-cacerts && chmod 0600 /var/lib/ca-certificates/java-cacerts

USER ${uid}:${gid}

RUN mkdir certs
RUN chmod 700 certs

ENTRYPOINT ["sh", "-c", "/home/appuser/entryPoint.sh $ENV_ACTIVE_PROFILE"]

EXPOSE 8080 8443
