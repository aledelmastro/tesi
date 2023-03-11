FROM maven:3.9.0-amazoncorretto-11 AS build
COPY src /usr/src/app/src
COPY pom.xml /usr/src/app
RUN mvn -f /usr/src/app/pom.xml clean package -Dmaven.test.skip

FROM openjdk:11
COPY --from=build /usr/src/app/target/otp-2.1.0-shaded.jar /otp-2.1.0-shaded.jar
COPY /data /data
EXPOSE 8080
ENTRYPOINT ["java","-jar","/otp-2.1.0-shaded.jar"]
CMD ["--load", "./data"]


# impostare java 11
# sudo update-alternatives --config java
# mvn clean package -DskipTests

# docker build -t registry:5000/otp2:sustain  .   ;      docker push   registry:5000/otp2:sustain
