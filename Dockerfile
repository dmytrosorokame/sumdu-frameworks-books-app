FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copy the built JAR file
# Note: Render will build the JAR using "mvn clean package" before docker build
# If building locally, run: mvn clean package first
COPY target/spring-boot-books-app-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]

