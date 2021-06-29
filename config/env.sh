
# Set Env ########################
##################################
if [ -z $HTTP_PORT ]; then export HTTP_PORT="8080"; fi
sed -i "s/%HTTP_PORT%/${HTTP_PORT}/g" /home/tmax/application.properties

if [ -z $DB_IP ]; then export DB_IP="192.168.153.174"; fi
sed -i "s/%DB_IP%/${DB_IP}/g" /home/tmax/application.properties

if [ -z $DB_PORT ]; then export DB_PORT="8629"; fi
sed -i "s/%DB_PORT%/${DB_PORT}/g" /home/tmax/application.properties

if [ -z $DB_USER ]; then export DB_USER="wapl"; fi
sed -i "s/%DB_USER%/${DB_USER}/g" /home/tmax/application.properties

if [ -z $DB_PASSWD ]; then export DB_PASSWD="tibero"; fi
sed -i "s/%DB_PASSWD%/${DB_PASSWD}/g" /home/tmax/application.properties

if [ -z $TRITON_IP ]; then export TRITON_IP="192.168.158.31"; fi
sed -i "s/%TRITON_IP%/${TRITON_IP}/g" /home/tmax/triton.properties

if [ -z $TRITON_PORT ]; then export TRITON_PORT="8004"; fi
sed -i "s/%TRITON_PORT%/${TRITON_PORT}/g" /home/tmax/triton.properties

if [ -z $TRITON_MODEL_NAME ]; then export TRITON_MODEL_NAME="knowledge-tracing"; fi
sed -i "s/%TRITON_MODEL_NAME%/${TRITON_MODEL_NAME}/g" /home/tmax/triton.properties

if [ -z $TRITON_MODEL_VER ]; then export TRITON_MODEL_VER="1"; fi
sed -i "s/%TRITON_MODEL_VER%/${TRITON_MODEL_VER}/g" /home/tmax/triton.properties

if [ -z $TRITON_WAPLSCORE_HOST ]; then export TRITON_WAPLSCORE_HOST="http://192.168.158.31:18000/v2/models/wapl-score/infer"; fi
sed -i "s~%TRITON_WAPLSCORE_HOST%~${TRITON_WAPLSCORE_HOST}~g" /home/tmax/triton.properties

if [ -z $LRS_IP ]; then export LRS_IP="192.168.153.132"; fi
sed -i "s/%LRS_IP%/${LRS_IP}/g" /home/tmax/lrs.properties

if [ -z $LRS_PORT ]; then export LRS_PORT="8080"; fi
sed -i "s/%LRS_PORT%/${LRS_PORT}/g" /home/tmax/lrs.properties
