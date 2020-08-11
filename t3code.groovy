job("GroovyCodeInterpreter") {
steps {


scm {
      github("Aaditya55/DT6", "master")
    }
triggers {
      scm("* * * * *")
    }
shell("sudo cp -rvf * /root/DT6")
if(shell("ls /root/DT6/ | grep html")) {
      dockerBuilderPublisher {
            dockerFileDirectory("/root/DT6/")
            cloud("Kube_slave")
tagsString("aaditya5/aadihtml:v1")
            pushOnSuccess(true)
      
            fromRegistry {
                  url("aaditya5")
                  credentialsId("xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx")
            }
            pushCredentialsId("xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx")
            cleanImages(false)
            cleanupWithJenkinsJobDelete(false)
            noCache(false)
            pull(true)
      }
}
else {
      dockerBuilderPublisher {
            dockerFileDirectory("/root/dt6/")
            cloud("Kube_slave")
tagsString("aaditya5/php:v1")
            pushOnSuccess(true)
      
            fromRegistry {
                  url("aaditya5")
                  credentialsId("xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx")
            }
            pushCredentialsId("xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx")
            cleanImages(false)
            cleanupWithJenkinsJobDelete(false)
            noCache(false)
            pull(true)
      }
}
 }
}


job("GroovyDeployment") {


  triggers {
    upstream {
      upstreamProjects("GroovyCodeInterpreter")
      threshold("SUCCESS")
    }  
  }


  steps {
    if(shell("ls /root/DT6 | grep html")) {


      shell("if sudo kubectl get pv html-pv; then if sudo kubectl get pvc html-pv-claim; then echo "Given volume present"; else kubectl create -f html-pv-claim.yml; fi; else sudo kubectl create -f html-pv.yml; sudo kubectl create -f html-pv-claim.yml; fi; if sudo kubectl get deployments html-deploy; then sudo kubectl rollout restart deployment/html-deploy; sudo kubectl rollout status deployment/html-deploy; else sudo kubectl create -f webdeploy-html.yml; sudo kubectl create -f webserver_expose.yml; sudo kubectl get all; fi")       


  }


    else {


      shell("if sudo kubectl get pv php-pv; then if sudo kubectl get pvc php-pv-claim; then echo "volume present"; else kubectl create -f php-pv-claim.yml; fi; else sudo kubectl create -f php-pv.yml; sudo kubectl create -f php-pv-claim.yml; fi; if sudo kubectl get deployments php-deploy; then sudo kubectl rollout restart deployment/php-deploy; sudo kubectl rollout status deployment/php-deploy; else sudo kubectl create -f webdeploy-php.yml; sudo kubectl create -f webserver_expose.yml; sudo kubectl get all; fi")


    }
  }
}


job("Monitor") {
  
  triggers {
     scm("* * * * *")
   }


steps {
    
    shell('export status=$(curl -siw "%{http_code}" -o /dev/null 192.168.99.100:50000); if [ $status -eq 200 ]; then exit 0; else python3 mail.py; exit 1; fi')
  }
}


job("Redeployment") {


triggers {
    upstream {
      upstreamProjects("Monitor")
      threshold("FAILURE")
    }
  }
  
  
  publishers {
    postBuildScripts {
      steps {
        downstreamParameterized {
  	  	  trigger("GroovyCodeInterpreter")
        }
      }
    }
  }
}
