pipeline { 
    environment {
        eclipseLocation = $WORKSPACE/eclipse/eclipse
        dest = file:///$WORKSPACE/tmp/$repoName/

    }
    parameters {
    string(name: 'SOURCE', defaultValue: '--//--//--//--', description: 'Eclipse repository url')
    string(name: 'repoName', defaultValue: 'release', description: 'Desirable path of mirror after URL')
    }
    stages {
        stage('download and extract eclipse') {
            steps {
              sh wget https://download.springsource.com/release/ECLIPSE/2019-09/eclipse-java-2019-09-R-linux-gtk-x86_64.tar.gz && \
              tar xf eclipse-java-2019-09-R-linux-gtk-x86_64.tar.gz && \
              rm -rf eclipse-java-2019-09-R-linux-gtk-x86_64.tar.gz
            }
        }
        stage('download repository') {
            environment {
                DEST_SERVER = credentials('server16-ip')
            }
            steps ('download mirror') {
                $eclipseLocation -nosplash -verbose -application org.eclipse.equinox.p2.metadata.repository.mirrorApplication -source $SOURCE -destination $dest
                $eclipseLocation -nosplash -verbose -application org.eclipse.equinox.p2.artifact.repository.mirrorApplication -source $SOURCE -destination $dest
            }
        }
        stage ('push repository') {
            withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId:'server16', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) 
            environment {
                DEST_SERVER = credentials('server16-ip')
            }
            ssh-keyscan $server >> ~/.ssh/known_hosts
            sshpass -p $PASSWORD rsync -r  $WORKSPACE/tmp/* $USERNAME@$DEST_SERVER:/data/update-sites/mirrors
        }
    }
}
