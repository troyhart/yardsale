# yardsale

A sample application built on top of the Axon Framework. The point of this project is to demonstrate the axon framework along with some of the infrastructure and functionality you will need for a real project. This includes keycloak as an SSO service supporting Oauth2, a spring boot application that serves the API and acts as an Oauth2 Resource Server that will validate JWT bearer tokens from the sso service. Also, this project demonstrates the integration of subscription com.myco.item.api.queries with html5 Server Sent Events with heartbeat with a 15 second period. 

The infrastructure is defined in the [docker compose yaml](docker-compose.yml). After you launch the project you will need to create a user in the [keycload admin console](http://localhost:8999/auth). Be sure to grant the user both the `USER` and `SYSTEM_ADMIN` roles.

## Commands

[See swagger documentation](http://localhost:8081/swagger-ui.html) for the best most up to date Api information. However, below are the CURL commands. Also, you can import [the insomnia workspace](insomnia-workspace.json) into the insomnia rest client ([google it](http://bfy.tw/OKE9)) and find the commands there.

Authenticate:

```text
curl --request POST \
  --url http://localhost:8999/auth/realms/yardsale/protocol/openid-connect/token \
  --header 'authorization: Basic eWFyZHNhbGUtYXBwOg==' \
  --header 'content-type: application/x-www-form-urlencoded' \
  --data grant_type=password \
  --data username= <keycloak user name> \
  --data password= <keycloak password> \
  --data 'scope=openid email profile'
```

Check SSO User Info:

```text
curl --request GET \
  --url http://localhost:8999/auth/realms/yardsale/protocol/openid-connect/userinfo \
  --header 'authorization: Bearer <your.access.token>'
```

Access Token Info (ie. Info from JWT bearer token parsed by the resource server -- yardsale):

```text
curl --request GET \
  --url http://localhost:8081/userInfo \
  --header 'authorization: Bearer <your.access.token>'
```

Create a Yardsale User Profile:

```text
curl --request POST \
  --url http://localhost:8081/users/<keycloak.subject.id>/ \
  --header 'authorization: Bearer <your.access.token> \
  --header 'content-type: application/json' \
  --data '{
  "apiKey": "<some fake value for an external Api Key>",
  "userId": "<some fake value for an external User Id>"
}'
```

Fetch a Yardsale User Profile:

```text
curl --request GET \
  --url http://localhost:8081/users/<keycloak.subject.id> \
  --header 'authorization: Bearer <your.access.token>'
```
