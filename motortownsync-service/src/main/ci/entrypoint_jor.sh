#!/bin/bash

## ENVIROMENT
########################################################################################################################

export INSTANCE_NAME=${INSTANCE_NAME:-eos.ingesta.etl}
export PORT0=${PORT0:-"8080"}

## VAULT
########################################################################################################################

source /opt/stratio/stratio-kms/kms_utils.sh

declare -a VAULT_HOSTS
IFS_OLD=$IFS
IFS=',' read -r -a VAULT_HOSTS <<< "$VAULT_HOST"

# Approle login from role_id, secret_id
if [ "xxx$VAULT_TOKEN" == "xxx" ];
then
login \
&& echo "OK: Login in Vault" \
|| echo "Error: Login in Vault"
fi

export CERTIFICATES_PATH=/data

getCAbundle ${CERTIFICATES_PATH} "PEM" \
&& echo "OK: Getting ca-bundle" \
|| echo "Error: Getting ca-bundle"

## CROSSDATA
########################################################################################################################

#echo "certificados crossdata"
#getCert userland "${CROSSDATAUSER}" "${CROSSDATAUSER}" "JKS" ${CERTIFICATES_PATH}

#chmod 0600 ${CERTIFICATES_PATH}/*

## POSTGRES COMMUNITY
########################################################################################################################

export FQDN=${INSTANCE_NAME}

getCert "userland" \
${INSTANCE_NAME} \
${FQDN} \
"PEM" \
${CERTIFICATES_PATH} \
&& echo "OK: Getting ${FQDN} certificate" \
|| echo "Error: Getting ${FQDN} certificate"

openssl pkcs8 -topk8 -inform PEM -outform DER -in ${CERTIFICATES_PATH}/${FQDN}.key -out ${CERTIFICATES_PATH}/${FQDN}.key.pk8 -nocrypt

## MEMORY CONFIGURATION
########################################################################################################################

MARATHON_APP_RESOURCE_MEM=${MARATHON_APP_RESOURCE_MEM:-"1024.0"}
PERCENTAGE=${PERCENTAGE:-"100"}
MARATHON_APP_MEM_INT=${MARATHON_APP_RESOURCE_MEM%.*}
MARATHON_APP_CPUS=${MARATHON_APP_RESOURCE_CPUS%.*}
MAX_MEM=$((MARATHON_APP_MEM_INT * PERCENTAGE / 100))

echo Available memory: $MARATHON_APP_RESOURCE_MEM MB
echo