FROM docker.io/library/gradle:7.5-jdk17@sha256:deb7b59256192aed1f463ab5e698c509cc77ce5d4e421d8b89b5d99eb0b24538 AS build
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
