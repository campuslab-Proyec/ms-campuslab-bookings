FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copiamos primero el pom.xml para aprovechar cache de dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiamos el código fuente y compilamos
COPY src ./src
RUN mvn clean package -DskipTests -B

# ---------- Etapa 2: runtime ----------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Usuario no-root por seguridad
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copiamos solo el jar generado desde la etapa de build
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]