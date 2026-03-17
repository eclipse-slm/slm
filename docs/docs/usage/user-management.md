---
permalink: /docs/usage/user-management
---

# User Management

Eclipse Service Lifecycle Management uses [Keycloak](https://www.keycloak.org/) as the underlying user management system.
Users can be managed via the REST API of the Platform Management component.

## Create and delete users via Platform Management

The Platform Management API provides endpoints for creating and deleting users:

- `POST /users` - create a new user
- `DELETE /users/{username}` - delete an existing user by username

Base URL:

```text
https://<<your-slm-host>>/platform-management
```

For authentication, use API key or bearer token as described in the [API documentation](/docs/usage/api).

## Create a user

Endpoint:

```text
POST /users
```

The request body must contain the following fields:

- `username`
- `firstName`
- `lastName`
- `password`
- `isPasswordTemporary`
- `email`

Example request:

```sh
curl --request POST \
  --url http://<<your-slm-host>>/platform-management/users \
  --header 'Authorization: Bearer <<access_token>>' \
  --header 'Content-Type: application/json' \
  --data '{
	"username": "max.mustermann",
	"firstName": "Max",
	"lastName": "Mustermann",
	"password": "ChangeMe123!",
	"isPasswordTemporary": true,
	"email": "max.mustermann@example.org"
  }'
```

If `isPasswordTemporary` is set to `true`, the user is required to change the password on the next login.

## Delete a user

Endpoint:

```text
DELETE /users/{username}
```

Example request:

```sh
curl --request DELETE \
  --url https://<<your-slm-host>>/platform-management/users/max.mustermann \
  --header 'Authorization: Bearer <<access_token>>'
```

## Notes

- The API calls manage users in the SLM user management backed by Keycloak and configure other componentes which allow Keycloak authentication 
  (e.g., Vault, Consul). Simply adding a user in Keycloak is not enough to create a new user.
- The Swagger UI of Platform Management can be used to explore and test the endpoints:
```sh
 https://<<your-slm-host>>/platform-management/swagger-ui.html
```