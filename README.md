# Search-Service


[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
![Java Version](https://img.shields.io/badge/Java-17-blue)
![Spring Boot Version](https://img.shields.io/badge/Spring%20Boot-3-brightgreen)

<p align="center">
  <img src="docs/img/logo-sdk-white-transparent.png" alt="SDK LOGO" width="50%">
  <br>
  <em>A data platform for everyone</em>
</p>


## Table of Contents

- [Table of Contents](#table-of-contents)
- [Description](#description)
- [Getting Started](#getting-started)
    - [Prerequisites](#prerequisites)
    - [Setup](#setup)
    - [Configuration](#configuration)
    - [Usage](#usage)
- [Built With](#built-with)
- [Contributing](#contributing)
- [Changelog](#changelog)

---


## Description


The `Search-Service` is a component of the **Superb Data Kraken Platform (SDK)**. It abstracts the underlying search engine, providing a consistent interface
regardless of the search engine used.

The service provides endpoints that allows users to view indices, retrieve filter options, access metadata properties, and execute searches among others.
Although these endpoints can be utilized directly by users, their primary purpose is to integrate with the search module of
the [Superb Data Kraken Frontend](https://github.com/EFS-OpenSource/superb-data-kraken-frontend).

For a more detailed understanding of the broader context of the platform this project is used in, refer to
the [architecture documentation](https://github.com/EFS-OpenSource/superb-data-kraken-architecture). (TODO)

For instructions on how to deploy the Search-Service on an instance of the **SDK**, refer to
the [installation instructions](https://github.com/EFS-OpenSource/superb-data-kraken-install-instructions). (TODO)

Refer to [index.md](docs%2Findex.md) for more detailed but deployment specific documentation.


## Getting Started


Follow the instructions below to set up a local copy of the project for development and testing.


### Prerequisites

- jdk >= 17
- Maven 3.6.x (if you're not using the Maven wrapper)
- A running OpenSearch instance
- A running OIDC/OAuth2 provider instance

### Setup


To set up your local environment for development and testing, follow these steps:

1. Clone the repository:
   ```bash
   git clone https://github.com/EFS-OpenSource/superb-data-kraken-search.git
   cd superb-data-kraken-search
1. Set up the service configuration:
    ```bash
   cp src/main/resources/application-local-template.yml src/main/resources/application-local.yaml
   ```
   Configure the `application-local.yaml` file based on your local development setup. The OpenSearch instance and the OIDC provider instance need to be
   configured correctly for the search service to run as expected.
1. Run the service:
   ```bash
   ./mvnw spring-boot:run -Dspring-boot:run.profiles=local
1. After successful setup, you can test the service using the auto-generated API documentation at:
   ```
   https://localhost:8090/search/swagger-ui/index.html

### Configuration

- `sdk.oauth2.config-url`: Specifies the URL pointing to the OpenID Connect discovery document. This URL provides essential configuration details for OAuth2
  and OpenID Connect operations, enabling dynamic discovery of authentication server endpoints and supported features. It's instrumental in configuring the
  OAuth2-based security settings within the Spring Boot application's security plugin.
- Replace all placeholders enclosed in `$()`, e.g.:
    - `REALM`: the specific realm set up with the openid connect (oidc) provider.
    - `CLIENT_ID`: the unique identifier for the service account that the service utilizes.
    - `CLIENT_SECRET`: the confidential passphrase or key associated with the service account.

### Usage


Here are the essential commands you'll need to use the service:

- Build the service:
  ```bash
  ./mvnw package
- Executing the tests alone:
  ```bash
  ./mvnw test
- Run the Search-Service:
   ```bash
   ./mvnw spring-boot:run -Dspring-boot:run.profiles=local
- Build the service Docker image:
  ```bash
  docker build -t sdk/search-service .

## Contributing


See the [Contribution Guide](./CONTRIBUTING.md).


## Changelog


See the [Changelog](./CHANGELOG.md).
