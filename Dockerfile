# Etapa de construcción
FROM amazoncorretto:17 AS build

WORKDIR /app

# Copiar archivos necesarios y construir el JAR
COPY . .
RUN ./gradlew bootJar

# Etapa de ejecución (imagen final)
FROM amazoncorretto:17

WORKDIR /app

# Copiar solo el JAR generado en la etapa de construcción
COPY --from=build /app/build/libs/*.jar app.jar

# Exponer el puerto
EXPOSE 8080

# Ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]