# FHIR-based Clinical Trials Registry

## Development setup

```sh
npm install
docker-compose -f deploy/docker-compose.dev.yml up
```

This starts a FHIR-server pre-loaded with sample studies @ <http://localhost:8082/fhir>. If your FHIR server is running on a different address, you need to modify [.env.development](.env.development).

### Compiles and hot-reloads for development

```sh
npm run serve
```

### Compiles and minifies for production

```sh
npm run build
```

### Run unit tests

```sh
npm run test:unit
```

### Run end-to-end tests

```sh
npm run test:e2e
```

### Lints and fixes files

```sh
npm run lint
```

## Deployment

### Configuration

The application is configured via environment variables. The following options are available:

| Variable             | Explanation                                                                   | Default                      |
| -------------------- | ----------------------------------------------------------------------------- | ---------------------------- |
| FHIR_URL             | The URL of the FHIR server containing the studies as ResearchStudy resources. | `http://localhost:8082/fhir` |
| METRICS_BEARER_TOKEN | Bearer token to secure access to the `/metrics` endpoint (optional).          | `""`                         |
