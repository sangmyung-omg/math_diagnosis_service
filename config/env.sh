# Set Env ########################
##################################
if [ -z $HTTP_PORT ]; then export HTTP_PORT="8080"; fi
sed -i "s&%HTTP_PORT%&${HTTP_PORT}&g" /home/tmax/application.properties

if [ -z $DB_IP ]; then export DB_IP="192.168.153.174"; fi
sed -i "s&%DB_IP%&${DB_IP}&g" /home/tmax/application.properties

if [ -z $DB_PORT ]; then export DB_PORT="8629"; fi
sed -i "s&%DB_PORT%&${DB_PORT}&g" /home/tmax/application.properties

if [ -z $DB_USER ]; then export DB_USER="wapl"; fi
sed -i "s&%DB_USER%&${DB_USER}&g" /home/tmax/application.properties

if [ -z $DB_PASSWD ]; then export DB_PASSWD="tibero"; fi
sed -i "s&%DB_PASSWD%&${DB_PASSWD}&g" /home/tmax/application.properties

if [ -z $LOG_LEVEL ]; then export LOG_LEVEL="DEBUG"; fi
sed -i "s&%LOG_LEVEL%&${LOG_LEVEL}&g" /home/tmax/application.properties

if [ -z $TRITON_HOST ]; then export TRITON_HOST="http://192.168.159.62:28000"; fi
sed -i "s&%TRITON_HOST%&${TRITON_HOST}&g" /home/tmax/triton.properties

if [ -z $LRS_HOST ]; then export LRS_HOST="http://192.168.153.132:8080"; fi
sed -i "s&%LRS_HOST%&${LRS_HOST}&g" /home/tmax/lrs.properties

# JWT settings
if [ -z $JWT_DEBUG_MODE ]; then export JWT_DEBUG_MODE="true"; fi
sed -i "s&%JWT_DEBUG_MODE%&${JWT_DEBUG_MODE}&g" /home/tmax/application.properties

if [ -z $JWT_HS_SHARED_SECRET ]; then export JWT_HS_SHARED_SECRET="default"; fi
sed -i "s&%JWT_HS_SHARED_SECRET%&${JWT_HS_SHARED_SECRET}&g" /home/tmax/application.properties

#Kafka server settings
if [ -z $KAFKA_BOOTSTRAP_SERVERS ]; then export KAFKA_BOOTSTRAP_SERVERS=""; fi
sed -i "s&%KAFKA_BOOTSTRAP_SERVERS%&${KAFKA_BOOTSTRAP_SERVERS}&g" /home/tmax/application.properties

if [ -z $KAFKA_TOPIC_NAME ]; then export KAFKA_TOPIC_NAME="sap-backend-event"; fi
sed -i "s&%KAFKA_TOPIC_NAME%&${KAFKA_TOPIC_NAME}&g" /home/tmax/application.properties

#Logstash TCP settings
logstash.host=%LOGSTASH_TCP_HOST%
logstash.port=%LOGSTASH_TCP_PORT%

if [ -z $LOGSTASH_TCP_HOST ]; then export LOGSTASH_TCP_HOST=""; fi
sed -i "s&%LOGSTASH_TCP_HOST%&${LOGSTASH_TCP_HOST}&g" /home/tmax/application.properties

if [ -z $LOGSTASH_TCP_PORT ]; then export LOGSTASH_TCP_PORT=""; fi
sed -i "s&%LOGSTASH_TCP_PORT%&${LOGSTASH_TCP_PORT}&g" /home/tmax/application.properties

# Add additional profiles (Kafka, logstash) if env is present
if [ ! -z $USE_KAFKA_EVENT ]; 
then export ADDITIONAL_PROFILE_LIST=${ADDITIONAL_PROFILE_LIST}",useKafka"; 
fi

if [ ! -z $USE_LOGSTASH_TCP ]; 
then export ADDITIONAL_PROFILE_LIST=${ADDITIONAL_PROFILE_LIST}",useLogstashTCP"; 
fi
