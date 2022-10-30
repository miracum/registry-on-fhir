FROM index.docker.io/library/node:18.12.0@sha256:cef9966b19672effeafcf1a67b8add742c3e46ca7dd5532efff60820526c2e95 AS base
WORKDIR /app

COPY package-lock.json ./
COPY package.json ./

FROM base AS build-frontend
COPY frontend/package.json ./frontend/package.json

RUN npm clean-install --workspace=frontend

COPY frontend/ ./frontend/

ARG VERSION=0.0.0
ENV VUE_APP_VERSION=${VERSION} \
    NODE_ENV=production
RUN npm run build --workspace=frontend

FROM build-frontend AS test
RUN npm run test:unit --workspace=frontend

FROM base AS build-server
COPY server/package.json ./server/package.json
RUN npm clean-install --workspace=server
COPY server/index.js ./server/index.js

FROM gcr.io/distroless/nodejs:18@sha256:445038d9cca8af12a21095cb6060abd84aa392bad5fc6a126f86a426b8fe5093
WORKDIR /app
ENV NODE_ENV=production
USER 65532:65532
EXPOSE 8080/tcp
CMD ["/app/server/index.js"]

COPY --from=build-server /app/server server
COPY --from=build-server /app/node_modules server/node_modules
COPY --from=build-frontend /app/frontend/dist dist

# included for vulnerability scans
COPY package.json ./
COPY package-lock.json ./

ARG VERSION=0.0.0
ARG GIT_REF=""
LABEL org.opencontainers.image.authors="miracum.org" \
    org.opencontainers.image.source="https://github.com/miracum/registry-on-fhir" \
    org.opencontainers.image.version=${VERSION} \
    org.opencontainers.image.revision=${GIT_REF} \
    org.opencontainers.image.vendor="miracum.org" \
    org.opencontainers.image.title="registry-on-fhir-web" \
    org.opencontainers.image.description="Web fronted for the FHIR-based trials registry."
