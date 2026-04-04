FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY backend/pom.xml ./
RUN mvn -B -q dependency:go-offline

COPY backend/src ./src
RUN mvn -B clean package -DskipTests


FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
