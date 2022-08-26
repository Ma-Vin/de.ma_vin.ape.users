![Maven Workflow Action](https://github.com/Ma-Vin/de.ma_vin.ape.users/actions/workflows/maven.yml/badge.svg)

# APE Users
APE spring boot application for user authentication and groupings

:hourglass_flowing_sand: in progress

## Sonarcloud analysis

* [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Ma-Vin_de.ma_vin.ape.users&metric=alert_status)](https://sonarcloud.io/dashboard?id=Ma-Vin_de.ma_vin.ape.users)
* [![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=Ma-Vin_de.ma_vin.ape.users&metric=reliability_rating)](https://sonarcloud.io/dashboard?id=Ma-Vin_de.ma_vin.ape.users)  [![Bugs](https://sonarcloud.io/api/project_badges/measure?project=Ma-Vin_de.ma_vin.ape.users&metric=bugs)](https://sonarcloud.io/dashboard?id=Ma-Vin_de.ma_vin.ape.users)
* [![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=Ma-Vin_de.ma_vin.ape.users&metric=security_rating)](https://sonarcloud.io/dashboard?id=Ma-Vin_de.ma_vin.ape.users)  [![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=Ma-Vin_de.ma_vin.ape.users&metric=vulnerabilities)](https://sonarcloud.io/dashboard?id=Ma-Vin_de.ma_vin.ape.users)
* [![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=Ma-Vin_de.ma_vin.ape.users&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=Ma-Vin_de.ma_vin.ape.users)  [![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=Ma-Vin_de.ma_vin.ape.users&metric=sqale_index)](https://sonarcloud.io/dashboard?id=Ma-Vin_de.ma_vin.ape.users)  [![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=Ma-Vin_de.ma_vin.ape.users&metric=code_smells)](https://sonarcloud.io/dashboard?id=Ma-Vin_de.ma_vin.ape.users)
* [![Coverage](https://sonarcloud.io/api/project_badges/measure?project=Ma-Vin_de.ma_vin.ape.users&metric=coverage)](https://sonarcloud.io/dashboard?id=Ma-Vin_de.ma_vin.ape.users)
* [![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=Ma-Vin_de.ma_vin.ape.users&metric=duplicated_lines_density)](https://sonarcloud.io/dashboard?id=Ma-Vin_de.ma_vin.ape.users)  [![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=Ma-Vin_de.ma_vin.ape.users&metric=ncloc)](https://sonarcloud.io/dashboard?id=Ma-Vin_de.ma_vin.ape.users)

## Profiles
* ***jwt-verification-token***: verifies the payload of the token against its signature
* ***database-token***: persists the issued tokens and refresh tokens at a database table *Tokens*. 
In this case it is possible to use more than one application instance.
* ***memory-token***: holds the issued tokens and refresh tokens at a map. 
In this case it is not possible to share them between different application instances.