FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -q -DskipTests dependency:go-offline

COPY src/ src/
RUN ./mvnw -DskipTests clean package

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY --from=build /app/target/webdiem-0.0.1-SNAPSHOT.war app.war
COPY uploads/ /app/seed-uploads/

ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080

CMD ["sh", "-c", "mkdir -p \"$APP_UPLOAD_DIR\" && if [ -d /app/seed-uploads ]; then cp -Rn /app/seed-uploads/. \"$APP_UPLOAD_DIR\"; fi && java $JAVA_OPTS -Dserver.port=${PORT} -jar /app/app.war"]
