FROM index.docker.io/library/gradle:7.5-jdk17@sha256:c0d2168f356d52b0ed3596498b222f43d458c1a90365f34708fd89a9810b0ae2 AS build
WORKDIR /home/gradle/src
COPY --chown=gradle:gradle . .

RUN gradle build --info && \
    gradle jacocoTestReport && \
    awk -F"," '{ instructions += $4 + $5; covered += $5 } END { print covered, "/", instructions, " instructions covered"; print 100*covered/instructions, "% covered" }' build/jacoco/coverage.csv && \
    java -Djarmode=layertools -jar build/libs/*.jar extract

FROM gcr.io/distroless/java17-debian11:nonroot@sha256:042cd6bf4bd22ea86c13097f4d2120ef51ba032fe54785b42758371b9d41ecdb
WORKDIR /opt/multi-site-merger
USER 65532:65532
EXPOSE 8080/tcp
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "org.springframework.boot.loader.JarLauncher"]

COPY --from=build /home/gradle/src/dependencies/ ./
COPY --from=build /home/gradle/src/spring-boot-loader/ ./
COPY --from=build /home/gradle/src/snapshot-dependencies/ ./
COPY --from=build /home/gradle/src/application/ .

ARG VERSION=0.0.0
ENV APP_VERSION=${VERSION} \
    SPRING_PROFILES_ACTIVE=prod

ARG GIT_REF=""
LABEL maintainer="miracum.org" \
    org.opencontainers.image.authors="miracum.org" \
    org.opencontainers.image.source="https://github.com/miracum/registry-on-fhir" \
    org.opencontainers.image.version=${VERSION} \
    org.opencontainers.image.revision=${GIT_REF} \
    org.opencontainers.image.vendor="miracum.org" \
    org.opencontainers.image.title="registry-on-fhir-multi-site-merger" \
    org.opencontainers.image.description="MIRACUM Research Study Multisite Merger."
