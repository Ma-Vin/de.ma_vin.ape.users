#!/bin/bash

echo "start app"
java -Djavax.net.ssl.trustStore=${client_trusted_keystore} -Djavax.net.ssl.trustStorePassword=${client_trusted_keystore_pwd} org.springframework.boot.loader.JarLauncher
echo "app stopped"
exit 1