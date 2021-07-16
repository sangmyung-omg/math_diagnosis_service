FROM openjdk:8u111-jdk-alpine

#Dir set
WORKDIR /home/tmax

#환경변수
ENV SCRIPT_HOME /home/tmax/script

RUN mkdir -p ${SCRIPT_HOME}

#Setting
COPY config/start.sh ${SCRIPT_HOME}/start.sh
COPY config/env.sh ${SCRIPT_HOME}/env.sh
COPY config/application.properties /home/tmax/application.properties
COPY config/triton.properties /home/tmax/triton.properties
COPY config/lrs.properties /home/tmax/lrs.properties
COPY config/statistics_module.properties /home/tmax/statistics_module.properties
COPY WaplMath/src/main/resources/statistics /home/tmax/statistics

RUN chmod -R 755 /home/tmax/script

ADD /WaplMath/build/libs/*.jar /home/tmax/app.jar

#ENTRYPOINT ["/bin/sh", "--"]
ENTRYPOINT ["./script/start.sh"]