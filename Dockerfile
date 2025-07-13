# Stage 1: Build the application using Maven
FROM eclipse-temurin:21-jdk-jammy as builder

WORKDIR /app

# Copy the Maven wrapper and pom.xml to leverage Docker layer caching
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Download dependencies
RUN ./mvnw dependency:go-offline

# Copy the rest of the source code and build the application
COPY src ./src
RUN ./mvnw package -DskipTests

# Stage 2: Create the final, smaller runtime image
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]