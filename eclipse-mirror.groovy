pipeline { 
    agent any
    
    environment {
        eclipseLocation = '$WORKSPACE/eclipse/eclipse'
        dest = 'file:///$WORKSPACE/tmp/$repoName/'

    }
    parameters {
    string(name: 'SOURCE', defaultValue: 'https://download.eclipse.org/nebula/releases/2.1.0/', description: 'Eclipse repository url')
    string(name: 'repoName', defaultValue: 'nebula/test/2.1.0', description: 'Desirable path of mirror after URL')
    }
    stages {
        stage('download and extract eclipse') {
            steps {
              sh """ 
              wget https://download.springsource.com/release/ECLIPSE/2019-09/eclipse-java-2019-09-R-linux-gtk-x86_64.tar.gz 
              tar xf eclipse-java-2019-09-R-linux-gtk-x86_64.tar.gz 
              rm -rf eclipse-java-2019-09-R-linux-gtk-x86_64.tar.gz
              """
            }
        }
        stage('download repository') {
            steps ('download mirror') {
                script {
                    sh """
                    $eclipseLocation -nosplash -verbose -application org.eclipse.equinox.p2.metadata.repository.mirrorApplication -source $SOURCE -destination $dest
                    $eclipseLocation -nosplash -verbose -application org.eclipse.equinox.p2.artifact.repository.mirrorApplication -source $SOURCE -destination $dest
                    """
                }
            }
        }
        stage ('push repository') {
            
            environment {
                DEST_SERVER = credentials('server16-ip')
            }
            steps ('Upload repository to server') {
                script {
                withCredentials([
                usernamePassword(credentialsId: "server16", usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')
]) {
                    sh """
                    ssh-keyscan $DEST_SERVER >> ~/.ssh/known_hosts
                    ~bin/sshpass -p $PASSWORD ~/bin/rsync -r  $WORKSPACE/tmp/* $USERNAME@$DEST_SERVER:/data/update-sites/mirrors
                    rm -rf $WORKSPACE/tmp/*
                    """
                    }
                }
            }
        }
    }
}
