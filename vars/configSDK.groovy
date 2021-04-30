def call(){

  sh """
    cd runtime
    cat ./src/main/resources/config.properties
    
    echo " "  >> ./src/main/resources/config.properties
    echo jdbc.url=jdbc:mysql://$HOST:$DB_PORT >> ./src/main/resources/config.properties
    echo prophet.workspace=$ENV_WORKSPACE >> ./src/main/resources/config.properties
    echo prophet.accessKey=$ACCESSKEY  >> ./src/main/resources/config.properties

    if [ -z $ENV_PORT ];then
       echo prophet.url=http://$HOST >> ./src/main/resources/config.properties
    else
       echo prophet.url=https://$HOST:$ENV_PORT >> ./src/main/resources/config.properties
    fi

    cat ./src/main/resources/config.properties

    """
}
