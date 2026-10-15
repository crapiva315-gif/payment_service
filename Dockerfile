
# ---------- Build stage ----------
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

COPY src src

RUN ./mvnw -B clean package -DskipTests

# ---------- Extract Spring Boot layers ----------
FROM eclipse-temurin:21-jre-alpine AS layers

WORKDIR /application

COPY --from=build /workspace/target/*.jar application.jar

RUN java -Djarmode=tools -jar application.jar extract \
    --layers --launcher --destination extracted

# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /application

USER spring:spring

COPY --from=layers /application/extracted/dependencies/ ./
COPY --from=layers /application/extracted/spring-boot-loader/ ./
COPY --from=layers /application/extracted/snapshot-dependencies/ ./
COPY --from=layers /application/extracted/application/ ./

EXPOSE 8084

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
