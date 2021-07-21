# Set Env ########################
##################################
if [ -z $HTTP_PORT ]; then export HTTP_PORT="8080"; fi
sed -i "s&%HTTP_PORT%&${HTTP_PORT}&g" /home/tmax/application.properties

if [ -z $DB_IP ]; then export DB_IP="220.90.208.175"; fi
sed -i "s&%DB_IP%&${DB_IP}&g" /home/tmax/application.properties

if [ -z $DB_PORT ]; then export DB_PORT="8629"; fi
sed -i "s&%DB_PORT%&${DB_PORT}&g" /home/tmax/application.properties

if [ -z $DB_USER ]; then export DB_USER="wapl"; fi
sed -i "s&%DB_USER%&${DB_USER}&g" /home/tmax/application.properties

if [ -z $DB_PASSWD ]; then export DB_PASSWD="tibero"; fi
sed -i "s&%DB_PASSWD%&${DB_PASSWD}&g" /home/tmax/application.properties

if [ -z $TRITON_HOST ]; then export TRITON_HOST="http://192.168.159.62:28000"; fi
sed -i "s&%TRITON_HOST%&${TRITON_HOST}&g" /home/tmax/triton.properties

if [ -z $LRS_HOST ]; then export LRS_HOST="http://192.168.153.132:8080"; fi
sed -i "s&%LRS_HOST%&${LRS_HOST}&g" /home/tmax/lrs.properties
