pipeline { 
    agent any

    // set eclipse locating and temp folder for repository
    environment {
        eclipseLocation = '$WORKSPACE/eclipse/eclipse'
        dest = 'file:///$WORKSPACE/tmp/$repoName/'
    }

    // basic parameters
    // TO DO : add parameters with secrets in jenkinsfile 
    parameters {
        string(name: 'source', defaultValue: 'https://download.eclipse.org/nebula/releases/2.1.0/', description: 'Eclipse repository url')
        string(name: 'repoName', defaultValue: 'nebula/releases/2.1.0', description: 'Desirable path of mirror after URL')
        string(name: 'sitePath', defaultValue: '/data/update-sites/mirrors/', description: 'path to server mirror store directory')
        string(name: 'sshUsername', defaultValue: 'optimax', description: 'ssh username')
    }

    stages {
        // TO DO : add check if eclipse exist
        stage('Download and extract eclipse') {
            steps {
              sh """ 
              wget https://download.springsource.com/release/ECLIPSE/2019-09/eclipse-java-2019-09-R-linux-gtk-x86_64.tar.gz 
              tar xf eclipse-java-2019-09-R-linux-gtk-x86_64.tar.gz 
              rm -rf eclipse-java-2019-09-R-linux-gtk-x86_64.tar.gz
              """
            }
        }

        stage('Download repository') {
            steps ('download mirror') {
                script {
                    sh """
                    $eclipseLocation -nosplash -verbose -application org.eclipse.equinox.p2.metadata.repository.mirrorApplication -source $source -destination $dest
                    $eclipseLocation -nosplash -verbose -application org.eclipse.equinox.p2.artifact.repository.mirrorApplication -source $source -destination $dest
                    """
                }
            }
        }

        stage ('Push repository') {
            // set server ip from credentials
            environment {
                destServer = credentials('serverIp')
            }

            // TO DO : better use sshagent and rsa keys insted of password and sshpass
            steps ('Upload repository to server and clear tmp folder.') {
                script {
                    // use credentials from parameters
                    sshagent(credentials: ['p2-site-updates']) {
            
                        sh """
                        ssh-keyscan $destServer >> ~/.ssh/known_hosts
                        scp -r $WORKSPACE/tmp/* $sshUsername@$destServer:$sitePath
                        rm -rf $WORKSPACE/tmp/*
                        """
                    }
                }                
            }
        }
    }
}
