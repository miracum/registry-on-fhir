FROM docker.io/library/node:18.12.1@sha256:c47a2c61e635eb4938fcd56a1139b552300624b53e3eca06b5554a577f1842cf AS base
WORKDIR /app

COPY package-lock.json ./
COPY package.json ./

FROM base AS build-frontend
COPY frontend/package.json ./frontend/package.json

RUN npm clean-install --workspace=frontend

COPY frontend/ ./frontend/

ARG VERSION=2.1.11 # x-release-please-version
ENV VUE_APP_VERSION=${VERSION} \
    NODE_ENV=production
RUN npm run build --workspace=frontend && \
    npm run test:unit --workspace=frontend

FROM base AS build-server
COPY server/package.json ./server/package.json
RUN npm clean-install --workspace=server
COPY server/index.js ./server/index.js

FROM gcr.io/distroless/nodejs:18@sha256:b534f9b5528e69baa7e8caf7bcc1d93ecf59faa15d289221decf5889a2ed3877
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
