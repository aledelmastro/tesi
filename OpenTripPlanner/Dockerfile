FROM openjdk:11
COPY /target/otp-2.1.0-shaded.jar /otp-2.1.0-shaded.jar
COPY /data /data
EXPOSE 8080
ENTRYPOINT ["java","-jar","/otp-2.1.0-shaded.jar", "--build", "--serve", "/data"]

# impostare java 11
# sudo update-alternatives --config java
# mvn clean package -DskipTests

# docker build -t registry:5000/otp2:sustain  .   ;      docker push   registry:5000/otp2:sustain
