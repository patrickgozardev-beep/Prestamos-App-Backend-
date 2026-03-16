# ETAPA 1: Construcción (Build)
# Usamos Maven con Java 21 para compilar el proyecto
FROM maven:3.9.6-eclipse-temurin-21-slim AS build
WORKDIR /app

# Copiamos solo el pom.xml primero para descargar las dependencias (optimiza cache)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiamos el código fuente y compilamos el JAR
COPY src ./src
RUN mvn clean package -DskipTests

# ETAPA 2: Ejecución (Runtime)
# Usamos un JRE ligero de Java 21 para correr la app
FROM eclipse-temurin:21-jre-slim
WORKDIR /app

# Copiamos el archivo JAR generado desde la etapa anterior
COPY --from=build /app/target/prestamosapp-0.0.1-SNAPSHOT.jar app.jar

# Variables de entorno para optimizar la memoria en Railway/Render
# -Xmx300M: Limita el uso de memoria para que no te cierren el contenedor
ENV JAVA_TOOL_OPTIONS="-Xmx300M -Xss512k -XX:MaxMetaspaceSize=128M -Duser.timezone=America/Lima"

# Exponemos el puerto estándar de Spring Boot
EXPOSE 8080

# Comando para iniciar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]