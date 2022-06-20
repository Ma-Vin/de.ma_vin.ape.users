FROM registry.access.redhat.com/ubi9/ubi:9.0.0@sha256:0cfdf5fb1529791f172caa5a517ae216f7ec1796565160bf3b539d3a8912732b

LABEL name="de.ma_vin.ape.users"
LABEL version="1.0-SNAPSHOT"
LABEL parent.name="ubi9"
LABEL parent.version="9.0.0"

ARG TEMP_DIR=/opt/ape/temp
ARG APP_DIR=/opt/ape/users

# create directory for scripts
RUN mkdir -p ${TEMP_DIR}
RUN mkdir -p ${APP_DIR}
WORKDIR ${APP_DIR}

# copy resource inclunding scripts
COPY src/main/resources ${TEMP_DIR}

# Run scripts
RUN ${TEMP_DIR}/docker/os_start.sh
RUN ${TEMP_DIR}/docker/install_java.sh
RUN ${TEMP_DIR}/docker/os_end.sh

# Add app to resources and extract
COPY target/users-1.0-SNAPSHOT.jar ${TEMP_DIR}/users-1.0-SNAPSHOT.jar
RUN cd ${TEMP_DIR} &&  \
    java -Djarmode=layertools -jar ${TEMP_DIR}/users-1.0-SNAPSHOT.jar extract && \
    cd ${APP_DIR}

# Move app layers
RUN cp -r ${TEMP_DIR}/dependencies/* ${APP_DIR} && \
    cp -r ${TEMP_DIR}/snapshot-dependencies/* ${APP_DIR} && \
    cp -r ${TEMP_DIR}/spring-boot-loader/* ${APP_DIR} && \
    cp -r ${TEMP_DIR}/application/* ${APP_DIR}

# remove all temp files
RUN rm -rf ${TEMP_DIR}

# Add some dummy values
COPY target/keystore/keystore.p12 ${APP_DIR}/sampleKeystore/
COPY target/keystore/trusted_keystore.p12 ${APP_DIR}/sampleKeystore/
ENV server.ssl.key-store=${APP_DIR}/sampleKeystore/keystore.p12
ENV client.trusted.keystore=${APP_DIR}/sampleKeystore/trusted_keystore.p12
ENV client.trusted.keystore.pwd="changeit"


ENTRYPOINT ["java", "-Djavax.net.ssl.trustStore${client.trusted.keystore}", "-Djavax.net.ssl.trustStorePassword=${client.trusted.keystore.pwd}", "org.springframework.boot.loader.JarLauncher"]