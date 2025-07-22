# Dockerfile
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY target/insurances-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
