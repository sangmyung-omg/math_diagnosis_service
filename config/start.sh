#!/bin/sh

# setup timezone
apk --no-cache add tzdata
cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime
apk del tzdata

# setup envs
. /home/tmax/script/env.sh

# run java
exec java -Djava.security.egd=file:/dev/./urandom -Dspring.profiles.active=prod${ADDITIONAL_PROFILE_LIST} -Duser.timezone=Asia/Seoul -jar /home/tmax/app.jar
