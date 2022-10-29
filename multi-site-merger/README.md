# Multi-site ResearchStudy Merger

Given a FHIR server, this service makes sure that ResearchStudy resources with the same identifiers but different sites are merged into master studies.

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
docker build -t multi-site-merger:localbuild .
docker run -p 8080:8080 multi-site-merger:localbuild
```
