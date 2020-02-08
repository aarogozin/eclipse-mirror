pipeline { 
    environment {
        DEST_SERVER = credentials('server16-ip')
        DEST_PASSWORD = credentials('server16-pass')
        ECLIPSE_DEST = $WORKSPACE/eclipse/eclipse
    }
    parameters {
    string(name: 'SOURCE', defaultValue: '--//--//--//--', description: 'Eclipse repository url')
    string(name: 'DEST', defaultValue: 'release', description: 'Desirable path of mirror after URL')
    }
    stages {
        stage('download and extract eclipse') {
            steps {
              sh wget https://download.springsource.com/release/ECLIPSE/2019-09/eclipse-java-2019-09-R-linux-gtk-x86_64.tar.gz && \
              tar xf eclipse-java-2019-09-R-linux-gtk-x86_64.tar.gz && \
              rm -rf eclipse-java-2019-09-R-linux-gtk-x86_64.tar.gz
            }
        }
        stage('mirror') {
            environment {
                DEST_SERVER = credentials('server16-ip')
            }
            steps {
                
                // 
            }
        }
    }
}