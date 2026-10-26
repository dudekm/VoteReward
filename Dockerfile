# Build and test image: JDK + Maven, nothing else needed on the host.
#   docker compose run --rm build
FROM maven:3.9-eclipse-temurin-21

WORKDIR /workspace

# Resolve dependencies in their own layer so source changes don't re-download them.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline || true

COPY src ./src

CMD ["mvn", "-B", "verify"]
