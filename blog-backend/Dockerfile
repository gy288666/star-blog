# ---- build ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package

# ---- run ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/blog-backend-1.0.0.jar app.jar
ENV TZ=Asia/Shanghai
EXPOSE 8080
VOLUME ["/data/uploads"]
ENTRYPOINT ["java", "-jar", "app.jar"]
