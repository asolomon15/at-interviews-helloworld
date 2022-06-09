# Hello World Sample App pipeline
The Hello World Sample App pipeline was implemented using the jenkins ci bpipeline langauge.  You will find this pipeline called "jenkins.groovy" within the root of the source code here.  

## Pipeline for Dev/Prod
My approach on handling dev/prod environments would be to use an environment variable called "BUILD_ENV".
This environment variable would then get assigned DEV or PROD depending on the jenkins job executed.
Here is an example of tagging the build.  Notice the `$BUILD_ENV_$COMMIT_ID` 
```
$ docker build \
    --no-cache \
    --build-arg GIT_COMMIT=$COMMIT_ID \
    -t helloworld:$BUILD_ENV_$COMMIT_ID \
    -t 310228935478.dkr.ecr.us-west-2.amazonaws.com/helloworld:$BUILD_ENV_$COMMIT_ID \
    .
```

# Challenges with Project
One of my biggest challenges was overthinking some of the authentication via aws CLI but some quick reading helped. 

## What Took Time
I also had an issue with the passing the `COMMIT_ID` to multiple stages.  The quick solution was to just add the `COMMIT_ID` to all stages that required it.  Here is an example of this issue.
```
    environment {
        COMMIT_ID = """${sh(
            script: 'git rev-parse --verify --short HEAD',
            returnStdout: true
        )}"""
    }
```

## Possible improvements
I would have liked to add the BUILD_ENV to the Kubernetes address so that I could see  `dev` or `prod` within the url.
Here's an example 
```
k8s-antoines-hellowor-dev-09888de666-235855470.us-west-2.elb.amazonaws.com
```
```
k8s-antoines-hellowor-prod-09888de666-235855470.us-west-2.elb.amazonaws.com
```
