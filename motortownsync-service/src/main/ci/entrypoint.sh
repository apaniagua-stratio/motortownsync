#!/bin/bash

set -e

source /data/stratio/kms_utils.sh
source /data/stratio/b-log.sh

DOCKER_LOG_LEVEL=${DOCKER_LOG_LEVEL:-DEBUG}
eval LOG_LEVEL_${DOCKER_LOG_LEVEL}
B_LOG --stdout true # enable logging over stdout

export PORT0=${PORT0:-"8080"}

export VAULT_PORT=${VAULT_PORT:-"8200"}
export VAULT_HOST=${VAULT_HOST:-"localhost"}

declare -a VAULT_HOSTS
IFS_OLD=$IFS
IFS=',' read -r -a VAULT_HOSTS <<< "$VAULT_HOST"


declare -a MARATHON_ARRAY
OLD_IFS=$IFS
IFS='/' read -r -a MARATHON_ARRAY <<< "$MARATHON_APP_ID"
IFS=$OLD_IFS


MARATHON_SERVICE_NAME=${MARATHON_ARRAY[-1]}
MARATHON_SERVICE_NAME=$(echo $MARATHON_SERVICE_NAME | sed -E 's/(.*)-[0-9]{13}//')

# Approle login from role_id, secret_id
if [[ -z "${VAULT_TOKEN}" ]];
then
   INFO "motorsync2 Login in vault..."
   login
   if [[ ${code} -ne 0 ]];
   then
       ERROR "Something went wrong log in in vault. Exiting..."
       return ${code}
   fi
fi

INFO "motorsync2 Logged on vault."

#### Basic Authentication for config-server #######
#getPass userland ${CONFIG_SERVER_NAME} basicauth
#CONFIG_SERVER_NAME_UNDERSCORE=${CONFIG_SERVER_NAME//-/_}
#CONFIG_SERVER_NAME_UPPERCASE=${CONFIG_SERVER_NAME_UNDERSCORE^^}
#BASICAUTH_USER_VAR=${CONFIG_SERVER_NAME_UPPERCASE}_BASICAUTH_USER
#BASICAUTH_PASS_VAR=${CONFIG_SERVER_NAME_UPPERCASE}_BASICAUTH_PASS
#export BASICAUTH_USERNAME=${!BASICAUTH_USER_VAR}
#export BASICAUTH_PASSWORD=${!BASICAUTH_PASS_VAR}


getCert "userland" \
       "motortownwatcher-dev" \
       "motortownwatcher-dev" \
        "PEM" \
        "/data/stratio" \
&& echo "motortownwatcher OK: Getting certificate motortownwatcher-dev" \
|| echo "motortownwatcher Error: Getting certificate motortownwatcher-dev"

getCert "userland" \
       "motortown_sync_dev" \
       "motortown_sync_dev" \
        "PEM" \
        "/data/stratio" \
&& echo "motortownwatcher OK: Getting certificate motortown_sync_dev" \
|| echo "motortownwatcher Error: Getting certificate motortown_sync_dev"


#GET CA-BUNDLE for given CA $SSL_CERT_PATH/ca-bundle.pem
getCAbundle "/data/stratio" "PEM" \
    && echo "motortownimport OK: Getting ca-bundle"   \
    || echo "motortownimport Error: Getting ca-bundle"

INFO "[TRUSTSTORE-CONFIG] Getting accepted certs from vault"
getCAbundle "/data/stratio" JKS "truststore.jks"

CA_BUNDLE_PEM="/data/stratio/truststore.jks"

URL_DATASOURCE_GLOBAL="?prepareThreshold=0&ssl=true&sslmode=verify-full&sslcert=${POSTGRES_CERT}&sslrootcert=${CA_BUNDLE_PEM}&sslkey=${POSTGRES_KEY}"

CA_BUNDLE_PEM="/data/stratio/ca-bundle.pem"
openssl x509 -outform der -in ${CA_BUNDLE_PEM} -out ${CA_BUNDLE_PEM}.der

openssl pkcs8 -topk8 -inform PEM -outform DER -in /data/stratio/motortown_sync_dev.key -out /data/stratio/motortown_sync_dev.key.pk8 -nocrypt

export JVMCA_PASS="changeit"
INFO "[TRUSTSTORE-CONFIG] Adding certs to java trust store"
${JAVA_HOME}/bin/keytool -importkeystore -srckeystore "/data/stratio/truststore.jks" -destkeystore $JAVA_HOME/lib/security/cacerts \
-srcstorepass "$DEFAULT_KEYSTORE_PASS" -deststorepass "$JVMCA_PASS" -noprompt

export SPARTA_TLS_TRUSTSTORE_PASSWORD=$DEFAULT_KEYSTORE_PASS

${JAVA_HOME}/bin/keytool -noprompt -import -storepass "changeit" -file ${CA_BUNDLE_PEM}.der -alias ${MARATHON_SERVICE_NAME} -cacerts


HEAP_PERCENTAGE=${HEAP_PERCENTAGE:-"80"}
JAVA_TOOL_OPTIONS=${JAVA_TOOL_OPTIONS:-"-XX:+UseG1GC -XX:MaxRAMPercentage=${HEAP_PERCENTAGE} -XshowSettings:vm"}
java ${JAVA_TOOL_OPTIONS} -jar /data/app.jar ${JAVA_ARGS}


#java ${JAVA_TOOL_OPTIONS} -Djavax.net.debug=ssl:handshake -jar /data/app.jar ${JAVA_ARGS}


