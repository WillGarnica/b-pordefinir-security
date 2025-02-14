# Etapa de construcción
FROM amazoncorretto:17 AS build

WORKDIR /app

# Copiar archivos del proyecto
COPY . .

# Otorgar permisos de ejecución a gradlew
RUN chmod +x ./gradlew

# Construir el JAR
RUN ./gradlew bootJar

# Etapa de ejecución (imagen final)
FROM amazoncorretto:17

WORKDIR /app

# Copiar el JAR generado
COPY --from=build /app/build/libs/*.jar app.jar

# Exponer el puerto
EXPOSE 8080

# Ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]