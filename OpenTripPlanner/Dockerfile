FROM openjdk:11

ENV \
  OTP_ROOT='/var/otp/' \
  TZ='Europe/Rome' \
  JAVA_OPTS='-Xms4G -Xmx4G -Duser.timezone="Europe/Rome" -Djava.util.prefs.userRoot=/tmp/'


USER 1000:1000
WORKDIR ${OTP_ROOT}
COPY target/otp-2.1.0-shaded.jar otp.jar
COPY entrypoint.sh .
EXPOSE 8080
ENTRYPOINT ["./entrypoint.sh"]
CMD ["--help"]

# impostare java 11
# sudo update-alternatives --config java
# mvn clean package -DskipTests

# docker build -t registry:5000/otp2:sustain  .   ;      docker push   registry:5000/otp2:sustain
