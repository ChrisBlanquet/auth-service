# 1. Usamos una imagen ligera de Java 21 
FROM eclipse-temurin:21-jdk-alpine 

# 2. Creamos una carpeta de trabajo dentro del contenedor
WORKDIR /app

# 3. Copiamos tu .jar EXACTO que se acaba de generar
COPY target/auth-0.0.1-SNAPSHOT.jar app.jar

# 4. Exponemos el puerto (Asumo que este microservicio corre en el 8080, ajusta si es diferente)
EXPOSE 8080

# 5. Comando para arrancar el microservicio
ENTRYPOINT ["java", "-jar", "app.jar"]