FROM public.ecr.aws/amazoncorretto/amazoncorretto:17

WORKDIR /app

COPY ecommauth/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java","-jar","app.jar"]
