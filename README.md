# Hello World Sample App
Welcome!  If you're cloning this repo, in all likelihood you are starting the Build & Release Engineer Homework assignment.  We're so happy that you've made it this far in the process!  By now you should have received a message from the hiring manager with login credentials to our Candidate AWS Environment, and the specifics of the Homework Assignment.  The document you're reading now (this README) is intended to help get you into the AWS environment, and that your account has all the permissions it needs to test locally, and actually complete the assignment.  

# Recommended Tooling (for local deployment/testing)
We recommend having the following tools to hand: 

[Docker](https://www.docker.com/products/docker-desktop)

[AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html)

[Kubectl](https://kubernetes.io/docs/tasks/tools/install-kubectl-macos/#install-with-homebrew-on-macos)

[Helm](https://helm.sh)

# AWS Console Access
You should have received a username and (temporary) password from the hiring manager.  With that, you can log into the [AWS Console](at-interviews.signin.aws.amazon.com/) and take a look around, if you are so inclined.  

# AWS API Access
Presuming you're on a Linux or MacOS machine, you can create/edit the file `~/.aws/credentials`.  Add a new section similar to the following, substituting the example values for the ones shared by HR: 
```
[at-interviews]
aws_access_key_id =  AKIA....
aws_secret_access_key = IKGkr....
```

To verify, you should now be able to run the command:
```
$ aws --profile at-interviews \
    sts get-caller-identity
```
This should output something similar to: 
```
{
    "UserId": "AKIA....",
    "Account": "310228935478",
    "Arn": "arn:aws:iam::310228935478:user/your_user_name_here"
}
```

# Manual Build/Deploy Steps
Our toy application is already able to be built, pushed, and deployed locally. We've got the particulars crammed into the `local-deploy.sh` script, but if you'd prefer a longer-form rundown of what's going on where, read on! 

## Build

Confirm all desired changes to the toy application are committed locally (not necessarily pushed), and then:
```
$ export COMMIT_ID=$(git rev-parse --verify --short HEAD) # This gives us a short, unique tag that we'll use when building/tagging the Docker image

```
```
$ docker build \
    --no-cache \
    --build-arg GIT_COMMIT=$COMMIT_ID \
    -t helloworld:$COMMIT_ID \
    -t 310228935478.dkr.ecr.us-west-2.amazonaws.com/helloworld:$COMMIT_ID \
    .
```

We should now have a local container built and able to be run locally 'in the usual fashion'.

## Login to external services
We'll need to authenticate to some of the external services in order to send our container on its merry way: 

Elastic Container Repository
```
$ aws ecr get-login-password \
    --region us-west-2 \
    | docker login \
    --username AWS \
    --password-stdin \
    310228935478.dkr.ecr.us-west-2.amazonaws.com
```

Elastic Kubernetes Service
```
aws eks update-kubeconfig \
    --region us-west-2 \
    --name at-interviews-cluster
```

# Push
```
$ docker push \
    310228935478.dkr.ecr.us-west-2.amazonaws.com/helloworld:$COMMIT_ID
```
Using the credentials above, this sends our container to ECR where Kubernetes can pull it down and actually deploy it in the next step.  

# Deploy
We're using Helm to abstract away as much of the complexity of Kubernetes as we possibly can.  Presuming our container is safely in ECR (above), deployment to Kubernetes and all the associated wiring should be as simple as: 
```
helm upgrade \
    --install \
    --namespace $(whoami) \
    --create-namespace \
    helloworld \
    --set image.tag=$COMMIT_ID \
    helm/helloworld
```
That should plug n' chug for a minute, then spit out some `kubectl` commands that will have an Internet Accessible URL™ serving up the toy application (it may take up to 5 minutes for DNS to propagate, FWIW).  And that is the manual deploy process, annotated.  You shouldn't need to run everything command-by-command, as that's what the `local-deploy.sh` script is for, but hopefully that gives you some context helpful to completing the homework assignment.  

