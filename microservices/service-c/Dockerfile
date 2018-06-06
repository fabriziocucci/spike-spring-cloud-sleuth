FROM openjdk:8u151-jre-alpine
RUN mkdir /opt && mkdir /opt/app
COPY build/libs/service-b-*.jar /opt/app/service-b.jar
CMD /opt/shared/scripts/install-certificates.sh && java -jar /opt/app/service-b.jar