---
permalink: /docs/usage/users
---

# Users

Eclipse Service Lifecycle Management uses [Keycloak](https://www.keycloak.org/) as the user management system.
In Order to modify SLM users you have to log into keycloak.

## Add User

The following steps describe the process of adding a new user:

1. Open Keycloak Admin Console by entering the following URL in your Browser: http://{{ hostname-of-slm }}:7080/auth/admin
2. Login with the admin credentials of Keycloak (default: admin/password)
3. Switch to realm "fabos"

<img :src="$withBase('/img/figures/use/user-add-1.png')">

4. Switch to "users" view

<img :src="$withBase('/img/figures/use/user-add-2.png')">

5. Click "add user"
6. Enter desired username and click "create"

<img :src="$withBase('/img/figures/use/user-add-3.png')">

7. Switch to "credentials" tab

<img :src="$withBase('/img/figures/use/user-add-4.png')">

8. Click "set password"
9. Enter desired password and turn off temporary. Click "save" and confirm with "save password"

<img :src="$withBase('/img/figures/use/user-add-5.png')">

10. Go to SLM Web UI at http://{{ hostname-of-slm }}:8080 and login with new username/password
11. Check user details by opening the user profile

<img :src="$withBase('/img/figures/use/user-add-6.png')">

<img :src="$withBase('/img/figures/use/user-add-7.png')">

## Delete User

1. Open Keycloak Admin Console by entering the following URL in your Browser: http://{{ hostname-of-slm }}:7080/auth/admin
2. Login with the admin credentials of Keycloak (default: admin/password)
3. Switch to realm "fabos"
4. Switch to "users" view
5. Select user to be deleted and click "delete user"

<img :src="$withBase('/img/figures/use/user-delete-1.png')">

6. Confirm Delete by clicking "delete"