package org.eclipse.slm.platform_management.features.user_management.impl;

import org.eclipse.slm.platform_management.features.user_management.api.UserCreateRequest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserManagementRestControllerTest {

    @Mock
    private UserManager userManager;

    @Nested
    class GetUsersTests {

        @Test
        void getUsers_returnsOkAndDelegatesToManager() {
            var controller = new UserManagementRestController(userManager);
            var users = List.of(Map.<String, Object>of(
                    "username", "alice",
                    "firstName", "Alice",
                    "lastName", "Doe",
                    "email", "alice@example.org",
                    "admin", true
            ));
            when(userManager.getUsers()).thenReturn(users);

            var response = controller.getUsers();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(users, response.getBody());
            verify(userManager).getUsers();
        }
    }

    @Nested
    class CreateUserTests {

        @Test
        void createUser_returnsOkAndDelegatesToManager() {
            var controller = new UserManagementRestController(userManager);
            var request = new UserCreateRequest(
                    "Alice_Admin",
                    "Alice",
                    "Doe",
                    "secret",
                    false,
                    "alice@example.org",
                    false
            );

            var response = controller.createUser(request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(userManager).createUser(request);
        }
    }

    @Nested
    class MakeUserAdminTests {

        @Test
        void makeUserAdmin_returnsOkAndDelegatesToManager() {
            var controller = new UserManagementRestController(userManager);

            var response = controller.makeUserAdmin("alice");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(userManager).makeUserAdmin("alice");
        }
    }

    @Nested
    class DeleteUserTests {

        @Test
        void deleteUser_returnsOkAndDelegatesToManager() {
            var controller = new UserManagementRestController(userManager);

            var response = controller.deleteUser("alice");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(userManager).deleteUser("alice");
        }
    }
}

