# playground websocket
POC used to study the potential of web sockets.

**Main features**
- push notifications to and from the web browser
- message backlog on redis (in case the client is not available at the moment)
- web application with sticky session
- stateless websocket connection (can connect to a different node than the one of the web session)

## Run into local environment
this is a maven project, so you can build it using the command
```
mvn clean package
```
then you can start a Jetty application server with this command
```
mvn jetty:run
```
the application will be deployed at the following url: http://localhost:8080/playground/w/login

refers to [User Credential]{#user-credentials} section to get the login credentials.

> the application requires a Redis server available on localhost:6379 

## Run into local Docker environment
use this command to deploy the application within your local Docker environment
```
docker-compose up
```
the application will be deployed at the following url: http://localhost:8080/playground/w/login

refers to [User Credential]{#user-credentials} section to get the login credentials.

## Run into Kubernetes environment
use this command to deploy the application within a kubernetes cluster

```
kubectl apply -f .\kube-deployment.yaml    
```
the application will be deployed at the following url: http://localhost:8080/playground/w/login

refers to [User Credential]{#user-credentials} section to get the login credentials.

### User credentials
use this credential to log into the application

| Username | Password |
|----------|----------|
| user1    | user1    |
| user2    | user2    |
| user3    | user3    |
| user4    | user4    |
| user5    | user5    |
