# Multi-site ResearchStudy Merger

Given a FHIR server, this service makes sure that ResearchStudy resources with the same identifiers but different sites are merged into master studies.

This service is deployed as part of the entire registry stack. See [deploy/README.md](deploy/README.md) for more info.

## Development

Setup a FHIR server required for development:

```sh
docker-compose -f deploy/docker-compose.dev.yml
```

### Build & Run

#### Gradle

```sh
./gradlew build
./gradlew bootRun
```

#### Docker

```sh
docker build -t multisitemerger:localbuild .
docker run -p 8080:8080 multisitemerger:localbuild
```
