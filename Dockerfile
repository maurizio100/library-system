FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY gradlew gradlew.bat settings.gradle.kts build.gradle.kts ./
COPY gradle/ gradle/
COPY shared/ shared/
COPY catalog/ catalog/
COPY lending/ lending/
COPY application/ application/
RUN ./gradlew :application:bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/application/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
