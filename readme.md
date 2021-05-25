# Build image 
- Build the code `./gradlew clean build`
- Build docker image `docker build -t talk2abid/crd-poc:1.0.0 .
- Push image `docker push talk2abid/crd-poc:1.0.0`

**Note**: Please update the version every time you have changed code. The same version to be used in `crd.yml L#81`. 

# Run on local 
- Start minikube `minikube start --vm-driver=hyperkit`
- Set docker context `eval $(minikube docker-env)`
- Run `kubectl create -f crd.yaml`

