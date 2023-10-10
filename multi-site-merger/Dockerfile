FROM docker.io/library/gradle:7.5-jdk17@sha256:c0d2168f356d52b0ed3596498b222f43d458c1a90365f34708fd89a9810b0ae2 AS build
WORKDIR /home/gradle/src
COPY --chown=gradle:gradle . .

RUN gradle build --info && \
    gradle jacocoTestReport && \
    awk -F"," '{ instructions += $4 + $5; covered += $5 } END { print covered, "/", instructions, " instructions covered"; print 100*covered/instructions, "% covered" }' build/jacoco/coverage.csv && \
    java -Djarmode=layertools -jar build/libs/*.jar extract

FROM gcr.io/distroless/java17-debian11:nonroot@sha256:dc6e301e2731abe1670877491f0548616c29ff56a3dbef8fcda68113684bd19a
WORKDIR /opt/multi-site-merger
USER 65532:65532
EXPOSE 8080/tcp
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]

COPY --from=build /home/gradle/src/dependencies/ ./
COPY --from=build /home/gradle/src/spring-boot-loader/ ./
COPY --from=build /home/gradle/src/snapshot-dependencies/ ./
COPY --from=build /home/gradle/src/application/ .

ENV SPRING_PROFILES_ACTIVE=prod
