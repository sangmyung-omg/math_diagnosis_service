#!/bin/sh
. /home/tmax/script/env.sh

exec java -Djava.security.egd=file:/dev/./urandom -Dspring.profiles.active=prod -jar /home/tmax/app.jar