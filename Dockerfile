FROM openjdk:11-jre-slim
COPY build/libs/*.jar crd-poc.jar
ENTRYPOINT ["java","-jar","/crd-poc.jar"]
