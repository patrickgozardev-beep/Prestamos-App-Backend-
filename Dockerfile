# ETAPA 1: Construcción (Build)
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copiamos el pom.xml y descargamos dependencias
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiamos el código y compilamos
COPY src ./src
RUN mvn clean package -DskipTests

# ETAPA 2: Ejecución (Runtime)
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copiamos el JAR (Asegúrate de que el nombre coincida con tu pom.xml)
COPY --from=build /app/target/prestamosapp-0.0.1-SNAPSHOT.jar app.jar

ENV JAVA_TOOL_OPTIONS="-Xmx300M -Xss512k -XX:MaxMetaspaceSize=128M -Duser.timezone=America/Lima"

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

