//global variable
def call(){
    sh """
        cd jacoco_report
        sh buildpipeline.sh $JACOCO_SERVER_HOST "30633" "true"
       """
}

