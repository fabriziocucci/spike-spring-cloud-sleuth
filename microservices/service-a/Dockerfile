FROM openjdk:8u151-jre-alpine
RUN mkdir /opt && mkdir /opt/app
COPY build/libs/service-a-*.jar /opt/app/service-a.jar
CMD /opt/shared/scripts/install-certificates.sh && java -jar /opt/app/service-a.jar