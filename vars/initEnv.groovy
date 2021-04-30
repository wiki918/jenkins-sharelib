import utils.test

def call(){

    def remote = [:]
    remote.name = DEPLOY_ENV_NAME
    remote.host = DEPLOY_ENV_HOST
    remote.user = DEPLOY_ENV_USER
    remote.password = DEPLOY_ENV_PASSWORD
    remote.allowAnyHosts = DEPLOY_ENV_ALLOWANYHOSTS  
    sshCommand remote: remote, command: DEPLOY_ENV_COMMOND

}
