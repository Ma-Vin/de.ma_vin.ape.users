FROM adoptopenjdk/openjdk14 as builder
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} users-1.0-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","/users-1.0-SNAPSHOT.jar"]
RUN java -Djarmode=layertools -jar users-1.0-SNAPSHOT.jar extract

FROM adoptopenjdk/openjdk14
COPY --from=builder dependencies/ ./
COPY --from=builder snapshot-dependencies/ ./
COPY --from=builder spring-boot-loader/ ./
COPY --from=builder application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]