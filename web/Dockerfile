FROM node:14.8 as build
WORKDIR /app
COPY package*.json ./
RUN npm ci --no-optional
COPY . ./
ARG VERSION=0.0.0
ENV VUE_APP_VERSION=${VERSION}
RUN npm run build && \
    npm prune --no-optional --production

FROM node:14.8-alpine
WORKDIR /app
ENV NODE_ENV=production
COPY --from=build /app/server server
COPY --from=build /app/node_modules node_modules
COPY --from=build /app/dist dist
# used for vulnerability scans
COPY package*.json ./

USER node
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
