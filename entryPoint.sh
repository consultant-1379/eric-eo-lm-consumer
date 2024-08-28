#!/bin/bash
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

PROFILE=$@

CERTS_DIR=/tmp/certs
CERTS_MNT_DIR=/opt/lm-consumer/certs

if [ $TLS_ENABLED == "true" ]
then
  mkdir /tmp/certs

  echo "Adding CA root certificate to truststore..."
  keytool -storepass $TRUSTSTORE_PASSWORD -noprompt -trustcacerts \
          -importcert -file $CERTS_MNT_DIR/ca/ca.crt \
          -alias sip-tls-root-ca-cert\
          -keystore $CERTS_DIR/lm-consumer-cacerts

  echo "Copying client certificate and key..."
  cat $CERTS_MNT_DIR/client/clicert.pem > $CERTS_DIR/clicert.pem
  cat $CERTS_MNT_DIR/client/cliprivkey.pem > $CERTS_DIR/cliprivkey.pem
  cat $CERTS_MNT_DIR/server/servercert.pem > $CERTS_DIR/servercert.pem
  cat $CERTS_MNT_DIR/server/serverprivkey.pem > $CERTS_DIR/serverprivkey.pem

  echo "Converting client certificate and private key to PKCS12 format..."
  openssl pkcs12 -export -in $CERTS_DIR/clicert.pem \
                 -inkey $CERTS_DIR/cliprivkey.pem \
                 -password pass:$KEYSTORE_PASSWORD \
                 -name lm-consumer-clicert > $CERTS_DIR/consumer-keystore.p12

  echo "Converting server certificate and private key to PKCS12 format..."
  openssl pkcs12 -export -in $CERTS_DIR/servercert.pem \
                 -inkey $CERTS_DIR/serverprivkey.pem \
                 -password pass:$KEYSTORE_PASSWORD \
                 -name lm-consumer-servercert > $CERTS_DIR/servercert.p12

  echo "Importing server certificate to keystore..."
  keytool -importkeystore -noprompt \
          -srckeystore $CERTS_DIR/servercert.p12 \
          -destkeystore $CERTS_DIR/consumer-keystore.p12 \
          -srcstorepass $KEYSTORE_PASSWORD \
          -deststorepass $KEYSTORE_PASSWORD \
          -srcstoretype pkcs12 \
          -deststoretype pkcs12

  echo "Removing intermediate certificates and keys..."
  rm $CERTS_DIR/clicert.pem
  rm $CERTS_DIR/cliprivkey.pem
  rm $CERTS_DIR/servercert.pem
  rm $CERTS_DIR/serverprivkey.pem

  java -Djava.security.egd=file:/dev/./urandom \
       -Djavax.net.ssl.keyStore=$CERTS_DIR/consumer-keystore.p12 \
       -Djavax.net.ssl.keyStorePassword=$KEYSTORE_PASSWORD \
       -Djavax.net.ssl.trustStore=$CERTS_DIR/lm-consumer-cacerts \
       -Djavax.net.ssl.trustStorePassword=$TRUSTSTORE_PASSWORD \
       $PROFILE -jar /home/appuser/eric-eo-lm-consumer.jar
else
  java -Djava.security.egd=file:/dev/./urandom \
       $PROFILE -jar /home/appuser/eric-eo-lm-consumer.jar
fi

