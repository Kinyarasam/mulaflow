# Build stage
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app

# Install Maven and ensure proper permissions
RUN apt-get update && \
    apt-get install -y maven && \
    rm -rf /var/lib/apt/lists/* && \
    mkdir -p /app/src && \
    chmod -R 777 /app

# Copy only the files needed for dependency resolution first
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source with proper permissions
COPY src ./src
RUN chmod -R 777 /app/src

# Debug: Verify source files
RUN find /app/src -name "*.java" -type f

# Build with debug output
RUN mvn clean package -DskipTests -X

# Runtime stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy the built JAR
COPY --from=builder /app/target/mulaflow-*.jar app.jar

# Environment variables
# ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Run as non-root user
RUN groupadd -r spring && useradd -r -g spring spring && \
    chown spring:spring /app
USER spring

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]