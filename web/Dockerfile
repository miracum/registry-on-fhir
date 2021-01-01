FROM node:14.15-alpine AS base
WORKDIR /app

FROM base AS build
COPY package*.json ./
RUN npm ci --no-optional
COPY . ./
ARG VERSION=0.0.0
ENV VUE_APP_VERSION=${VERSION}
RUN npm run build

FROM build AS test
RUN npm run test:unit

FROM build AS pruned
RUN npm prune --production

FROM base
ENV NODE_ENV=production
COPY --from=pruned /app/server server
COPY --from=pruned /app/node_modules node_modules
COPY --from=pruned /app/dist dist
# included for vulnerability scans
COPY package*.json ./

USER 65532
EXPOSE 3000
HEALTHCHECK CMD wget --quiet --tries=1 --spider http://localhost:3000/health || exit 1
ENTRYPOINT [ "npm" ]
CMD ["run", "server:start"]

ARG VERSION=0.0.0
ARG GIT_REF=""
ARG BUILD_TIME=""
LABEL org.opencontainers.image.created=${BUILD_TIME} \
    org.opencontainers.image.authors="miracum.org" \
    org.opencontainers.image.source="https://gitlab.miracum.org/miracum/uc1/registry/web" \
    org.opencontainers.image.version=${VERSION} \
    org.opencontainers.image.revision=${GIT_REF} \
    org.opencontainers.image.vendor="miracum.org" \
    org.opencontainers.image.title="uc1-registry-web" \
    org.opencontainers.image.description="Web app for MIRACUM-wide central trials registry."
