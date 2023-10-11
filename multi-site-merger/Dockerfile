FROM docker.io/library/gradle:7.6-jdk17@sha256:c9be33dbbf9b692b0f36e4b832a1f1022a6876e03c1bdf369f21c2311bab1a6f AS build
WORKDIR /home/gradle/src
COPY --chown=gradle:gradle . .

RUN gradle build --info && \
    gradle jacocoTestReport && \
    awk -F"," '{ instructions += $4 + $5; covered += $5 } END { print covered, "/", instructions, " instructions covered"; print 100*covered/instructions, "% covered" }' build/jacoco/coverage.csv && \
    java -Djarmode=layertools -jar build/libs/*.jar extract

FROM gcr.io/distroless/java17-debian11:nonroot@sha256:0e823486cb2cf10a3528385e1f648ac6c3100806ed437eea62ebf2958edd98e4
WORKDIR /opt/multi-site-merger
USER 65532:65532
EXPOSE 8080/tcp
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]

COPY --from=build /home/gradle/src/dependencies/ ./
COPY --from=build /home/gradle/src/spring-boot-loader/ ./
COPY --from=build /home/gradle/src/snapshot-dependencies/ ./
COPY --from=build /home/gradle/src/application/ .

ENV SPRING_PROFILES_ACTIVE=prod
