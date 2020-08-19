# MIRACUM Research Study Multisite Merger

Given a FHIR server, this service makes sure that ResearchStudy resources with the same identifiers but different sites are merged into a master studies.

## Run

```sh
export IMAGE_TAG=$(git describe --abbrev=0 --tags) # get the latest version tag
export FHIR_URL=http://localhost:8082/fhir # specify the URL of a FHIR server containing the ResearchStudy resources
docker-compose -f deploy/docker-compose.yml up
```

See also the E2E tests in `tests/e2e/docker-compose.yml` for a complete, self-contained setup.

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

### Contribute

Setup commitlint to make sure your commits messages follow the conventional commit standard (optional during development but at least the squashed commit messages need to follow the convention):

```sh
npm install
```
