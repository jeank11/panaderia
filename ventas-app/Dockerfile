# Etapa 1: Construcción con Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copiar pom.xml y descargar dependencias
COPY pom.xml .
RUN mvn -q -e -B dependency:resolve

# Copiar el código fuente
COPY src ./src

# Construir el .jar
RUN mvn -q -e -B package

# Etapa 2: Imagen final más ligera
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Copiar el jar generado
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

