FROM eclipse-temurin:17-jdk AS builder
COPY . .
RUN ./gradlew build

FROM eclipse-temurin:17-jre-alpine
COPY --from=builder build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
