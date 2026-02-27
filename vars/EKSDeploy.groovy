    def call (Map configMap){
        def appVersion = configMap.get("appVersion")
        def project = configMap.get("project")
        def component = configMap.get("component")
        def deploy_to = configMap.get("deploy_to")

        pipeline {
    // These are pre-build sections
    
    agent {
        node {
            label 'Agent-1'
        }
    }
    environment {
        COURSE = "Jenkins"
        
        ACC_ID = "153402910823"
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
                        dir("${component}-deploy") {
                            sh """
                                set -e
                                aws eks update-kubeconfig --region ${REGION} --name ${project}-${deploy_to}
                                kubectl get nodes
                                sed -i "s/IMAGE_VERSION/${appVersion}/g" values.yaml
                                helm upgrade --install ${component} -f values-${deploy_to}.yaml -n ${project} --atomic --wait --timeout=5m .
                                
                            """
                        }
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