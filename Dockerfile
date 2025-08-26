# Usar OpenJDK 21 como imagen base
FROM openjdk:21-jdk-slim

# Establecer directorio de trabajo
WORKDIR /app

# Copiar archivos Maven wrapper
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Dar permisos de ejecución al wrapper
RUN chmod +x ./mvnw

# Descargar dependencias (para cache de Docker)
RUN ./mvnw dependency:resolve

# Copiar código fuente
COPY src ./src

# Construir la aplicación
RUN ./mvnw clean package -DskipTests

# Exponer puerto
EXPOSE 8080

# Comando para ejecutar la aplicación
CMD ["java", "-Dspring.profiles.active=prod", "-jar", "target/Sistema-digital-de-venta-de-libros-0.0.1-SNAPSHOT.jar"]