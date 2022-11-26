# web

The frontend and proxy server components of the FHIR-based trials registry.

## Development

Start a FHIR server filled with sample data:

```sh
docker compose -f docker-compose.dev.yaml up
```

Install packages

```sh
npm clean-install
```

Run the VueJS frontend in development mode:

```sh
npm run serve --workspace=frontend
```

Run the server component:

```sh
npm run start --workspace=server
```
