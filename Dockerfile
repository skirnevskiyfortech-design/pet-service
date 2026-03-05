FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Копируем обвязку Gradle (Wrapper)
COPY gradlew ./
COPY gradle ./gradle

# Копируем конфиги и исходники
COPY build.gradle.kts settings.gradle.kts ./
COPY src ./src

RUN chmod +x gradlew
RUN ./gradlew clean bootJar -x test

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
