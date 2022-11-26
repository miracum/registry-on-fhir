FROM docker.io/library/node:18.12.1@sha256:c47a2c61e635eb4938fcd56a1139b552300624b53e3eca06b5554a577f1842cf AS base
WORKDIR /app

COPY package-lock.json ./
COPY package.json ./

FROM base AS build-frontend
COPY frontend/package.json ./frontend/package.json

RUN npm clean-install --workspace=frontend

COPY frontend/ ./frontend/

ARG VERSION=2.1.10 # x-release-please-version
ENV VUE_APP_VERSION=${VERSION} \
    NODE_ENV=production
RUN npm run build --workspace=frontend && \
    npm run test:unit --workspace=frontend

FROM base AS build-server
COPY server/package.json ./server/package.json
RUN npm clean-install --workspace=server
COPY server/index.js ./server/index.js

FROM gcr.io/distroless/nodejs:18@sha256:4d7a90fba0aba8d143f011ded99268423b7bac5ea509e62216c9318eef2a7477
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
