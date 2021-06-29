FROM openjdk:8u111-jdk-alpine

#Dir set
WORKDIR /home/tmax

#환경변수
ENV SCRIPT_HOME = /home/tmax/script

#Setting
COPY config/start.sh ${SCRIPT_HOME}/start.sh
COPY config/env.sh ${SCRIPT_HOME}/env.sh

RUN chmod -R 755 /home/tmax/script

ADD /WaplMath/build/libs/*.jar /home/tmax/app.jar

ENTRYPOINT ["/bin/sh", "--"]
CMD ["script/start.sh"]