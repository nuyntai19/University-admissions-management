# syntax=docker/dockerfile:1

# Build stage
FROM maven:3.9.11-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml ./
COPY src ./src

# Build JAR + copy runtime dependencies (this is not a fat-jar project)
RUN mvn -DskipTests package \
    dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory=target/dependency


# Runtime stage
FROM eclipse-temurin:17-jre-jammy AS runtime

# Note: this is a Java Swing (GUI) app.
# Running GUI apps in Docker usually requires extra host setup (X server / GUI forwarding).

WORKDIR /app

COPY --from=build /workspace/target/PhanMemTuyenSinh-*.jar /app/app.jar
COPY --from=build /workspace/target/dependency /app/lib

# Database configuration (overrides hibernate.cfg.xml when provided)
ENV DB_HOST=host.docker.internal \
    DB_PORT=3306 \
    DB_NAME=xettuyen2026 \
    DB_USER=root \
    DB_PASS=12345678

CMD ["java", "-cp", "/app/app.jar:/app/lib/*", "vn.edu.sgu.phanmemtuyensinh.PhanMemTuyenSinh"]
