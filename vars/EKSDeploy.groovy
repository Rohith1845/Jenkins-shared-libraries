    def call (Map configMap){
        pipeline {
    // These are pre-build sections
    agent {
        node {
            label 'Agent-1'
        }
    }
    environment {
        COURSE = "Jenkins"
        appVersion = configMap.get("appVersion")
        ACC_ID = "131315333865"
        project = configMap.get("project")
        component = configMap.get("component")
        deploy_to = configMap.get("deploy_to")
        REGION = "us-east-1"
    }
    options {
        timeout(time: 10, unit: 'MINUTES') 
        disableConcurrentBuilds()
    }
    // This is build section
    
    stages { 
        stage('Deploy') {
            steps {
                script{
                    withAWS(region:'us-east-1',credentials:'aws-creds'){
                        sh """
                            set -e
                            aws eks update-kubeconfig --region ${REGION} --name ${project}-${deploy_to}
                            kubectl get nodes
                            sed -i "s/IMAGE_VERSION/${appVersion}/g" values.yaml
                            helm upgrade --install ${component} \
                            -f values-${deploy_to}.yaml \
                            -n ${project} \
                            --wait \
                            --timeout=5m \
                            .

                        """
                    }
                }
            }
        }   
        

    }
    post{
        always{
            echo 'I will always say Hello again!'
            cleanWs()
        }
        success {
            echo 'I will run if success'
        }
        failure {
            echo 'I will run if failure'
        }
        aborted {
            echo 'pipeline is aborted'
        }
    }
}
}