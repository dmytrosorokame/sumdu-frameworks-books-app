# Multi-stage build: compile and package
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

# Install Maven
RUN apk add --no-cache maven

# Copy pom.xml first (for better layer caching)
COPY pom.xml .

# Download dependencies (cached layer if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/target/spring-boot-books-app-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]

