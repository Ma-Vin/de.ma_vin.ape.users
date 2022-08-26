![Maven Workflow Action](https://github.com/Ma-Vin/de.ma_vin.ape.users/actions/workflows/maven.yml/badge.svg)

# APE Users

APE spring boot application for user authentication and groupings

:hourglass_flowing_sand: in progress

## sonarcloud analysis

* [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Ma-Vin_de.ma_vin.ape.users&metric=alert_status)](https://sonarcloud.io/dashboard?id=Ma-Vin_de.ma_vin.ape.users)
* [![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=Ma-Vin_de.ma_vin.ape.users&metric=reliability_rating)](https://sonarcloud.io/dashboard?id=Ma-Vin_de.ma_vin.ape.users)  [![Bugs](https://sonarcloud.io/api/project_badges/measure?project=Ma-Vin_de.ma_vin.ape.users&metric=bugs)](https://sonarcloud.io/dashboard?id=Ma-Vin_de.ma_vin.ape.users)
* [![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=Ma-Vin_de.ma_vin.ape.users&metric=security_rating)](https://sonarcloud.io/dashboard?id=Ma-Vin_de.ma_vin.ape.users)  [![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=Ma-Vin_de.ma_vin.ape.users&metric=vulnerabilities)](https://sonarcloud.io/dashboard?id=Ma-Vin_de.ma_vin.ape.users)
* [![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=Ma-Vin_de.ma_vin.ape.users&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=Ma-Vin_de.ma_vin.ape.users)  [![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=Ma-Vin_de.ma_vin.ape.users&metric=sqale_index)](https://sonarcloud.io/dashboard?id=Ma-Vin_de.ma_vin.ape.users)  [![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=Ma-Vin_de.ma_vin.ape.users&metric=code_smells)](https://sonarcloud.io/dashboard?id=Ma-Vin_de.ma_vin.ape.users)
* [![Coverage](https://sonarcloud.io/api/project_badges/measure?project=Ma-Vin_de.ma_vin.ape.users&metric=coverage)](https://sonarcloud.io/dashboard?id=Ma-Vin_de.ma_vin.ape.users)
* [![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=Ma-Vin_de.ma_vin.ape.users&metric=duplicated_lines_density)](https://sonarcloud.io/dashboard?id=Ma-Vin_de.ma_vin.ape.users)  [![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=Ma-Vin_de.ma_vin.ape.users&metric=ncloc)](https://sonarcloud.io/dashboard?id=Ma-Vin_de.ma_vin.ape.users)

## profiles

* ***jwt-verification-token***: verifies the payload of the token against its signature
* ***database-token***: persists the issued tokens and refresh tokens at a database table *Tokens*.
  In this case it is possible to use more than one application instance.
* ***memory-token***: holds the issued tokens and refresh tokens at a map.
  In this case it is not possible to share them between different application instances.

## configuration

The following sections are describing the possible configuration of the app. The shown values are the existing ones
of [application.yml](src/main/resources/application.yml) and should only be handled as an example configuration

:bangbang: change every secret of the default configuration :bangbang:

### database

A common spring boot database configuration is needed

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:mvapeuser
    driver-class-name: org.h2.Driver
    username: sa
    password:
  h2:
    console:
      enabled: false
      path: /h2-console
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
```

A h2 database is used as default. There exists also a *SecurityFilterChain*
at [SecurityConfig.h2ConsoleFilterChain](src/main/java/de/ma_vin/ape/users/security/SecurityConfig.java) which enable
and disable the console endpoint depending on *spring.h2.console.enabled* property

### jwt introspection

The endpoints, matching the regex *\/(group|user|admin)\/.** , are protected by a bearer token. This Token is verified
against some introspection endpoint.
This is implemented as *SecurityFilterChain*
at [SecurityConfig.oAuthResourceFilterChain](src/main/java/de/ma_vin/ape/users/security/SecurityConfig.java) and uses
the default opaque bearer token support of spring boot.

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        opaque:
          introspection-uri: https://localhost:8443/oauth/introspection
          introspection-client-id: users
          introspection-client-secret: f58497fe32e68138273588d18f62d92366a97bb451d573fb1860ce615e540e15
```

This app provides an own */oauth/introspection*
endpoint [AuthController.introspection](src/main/java/de/ma_vin/ape/users/controller/auth/AuthController.java) and the
default configuration uses this one. The
behavior of this endpoint can be configured through the profiles described above.

### tls

The app should be accessed via *https* and is configured through common spring boot mechanism

```yaml
server:
  port: 8443
  ssl:
    enabled: true
    key-store-type: PKCS12
    key-store: target/keystore/keystore.p12
    key-store-password: changeit
    key-alias: users
    protocol: TLS
    enabled-protocols: TLSv1.3
```

A keystore for integration testing will be created during the maven
build ([all ids at keytool-maven-plugin config](pom.xml])). This keystore is referenced as default. Although the
certificate is a signed one (and also not self-signed), never use this for any production scenario and issue it from
proven issuer!

### logging

*log4j2* is used as logging framework

```yaml
logging:
  level:
    root: INFO
```

The default logging severity is set to *INFO*

### initialization

There is some database initialization implemented
at [InitUserEventListener](src/main/java/de/ma_vin/ape/users/listener/InitUserEventListener.java). This is only some
addition to the default jpa spring boot initialization modes

```yaml
db:
  user:
    isToInitialize: true
    isCommonGroupToInitialize: true
    initUserWithDefaultPwd: false
```

If ***isToInitialize*** is not equal to *true* or there exists any entry at *USER*-table the initialization will be
skipped (The entry-check is needed if there are multiple app instances existing).
If there does not occur any skipping, an admin group *admins* and a global admin *global, admin* with pwd *admin* will
be created.

If ***isCommonGroupToInitialize*** is equal to *true* a common group *common* will be created also.

***initUserWithDefaultPwd*** is not supported at the moment

### token

In addition to ***jwt introspection*** the app provides jwt tokens by itself.

```yaml
tokenSecret: SomeSecret
tokenExpiresInSeconds: 300
refreshTokenExpiresInSeconds: 900
encodingAlgorithm: HS256
authorizeCodeExpiresInSeconds: 600
auth:
  tokenWithClientSecret: false
  clients:
    - clientId: users
      secret: f58497fe32e68138273588d18f62d92366a97bb451d573fb1860ce615e540e15
      url: https://localhost:8443
      redirects:
        - redirect-start: https://localhost:8443/
    - clientId: ape.user.ui
      secret: changeIt
      url: https://localhost:4200
      redirects:
        - redirect-start: https://localhost:4200/
```

***tokenSecret*** is the secret for the signature encryption of the jwt

***encodingAlgorithm*** algorithm to use for signature encryption. The following ones are supported at the moment

| value       | algorithm name | recommended against |
|-------------|----------------|---------------------|
| *MD5*       | HmacMD5        | :x:                 |
| *HS1*       | HmacSHA1       | :x:                 |
| *HS224*     | HmacSHA224     | :x:                 |
| *HS256*     | HmacSHA256     |                     |
| *HS384*     | HmacSHA384     |                     |
| *HS512*     | HmacSHA512     |                     |
| *HS512/224* | HmacSHA512/224 |                     |
| *HS512/256* | HmacSHA512/256 |                     |

***tokenExpiresInSeconds*** defines the seconds until the token should expire

***refreshTokenExpiresInSeconds*** defines the seconds until the refresh token should expire

***authorizeCodeExpiresInSeconds*** defines the seconds until the authorization code should expire

***tokenWithClientSecret*** if equal to *true* the endpoint */oauth/token* is basic auth protected with respect to
the ***clientId,secret*** pairs at *auth.clients* (
see [ClientCheckFilter](src/main/java/de/ma_vin/ape/users/security/filter/ClientCheckFilter.java))

At Least ***clientId, url*** are needed. If the app is used for authorization code also, the redirect urls are to name
too. In this case the values at *auth.clients.redirects.redirect-start* have to match the redirection request (Not only
to start with)