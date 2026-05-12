package org.eclipse.slm.platform_management.features.user_management.api;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UserCreateRequestValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void initValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        if (validatorFactory != null) {
            validatorFactory.close();
        }
    }

    private static UserCreateRequest validRequest() {
        return new UserCreateRequest(
                "Alice_Admin",
                "Alice",
                "Doe",
                "secret",
                false,
                "alice@example.org",
                false);
    }

    @Nested
    class UsernameValidationTests {

        @Test
        void acceptsUsername_whenItStartsWithLetterAndContainsAllowedCharacters() {
            var request = new UserCreateRequest("A1_user-name", "Alice", "Doe", "secret", false, "alice@example.org", false);

            var violations = validator.validate(request);

            assertTrue(violations.stream().noneMatch(v -> "username".equals(v.getPropertyPath().toString())));
        }

        @Test
        void rejectsUsername_whenItDoesNotStartWithLetter() {
            var request = new UserCreateRequest("1alice", "Alice", "Doe", "secret", false, "alice@example.org", false);

            var violations = validator.validate(request);

            assertTrue(violations.stream().anyMatch(v -> "username".equals(v.getPropertyPath().toString())));
        }
    }

    @Nested
    class FirstNameValidationTests {

        @Test
        void rejectsFirstName_whenItDoesNotStartWithLetter() {
            var request = new UserCreateRequest("Alice", "-Alice", "Doe", "secret", false, "alice@example.org", false);

            var violations = validator.validate(request);

            assertTrue(violations.stream().anyMatch(v -> "firstName".equals(v.getPropertyPath().toString())));
        }

        @Test
        void acceptsFirstName_whenItStartsWithLetterAndUsesAllowedCharacters() {
            var request = new UserCreateRequest("Alice", "A li-ce.", "Doe", "secret", false, "alice@example.org", false);

            var violations = validator.validate(request);

            assertTrue(violations.stream().noneMatch(v -> "firstName".equals(v.getPropertyPath().toString())));
        }
    }

    @Nested
    class LastNameValidationTests {

        @Test
        void rejectsLastName_whenItDoesNotStartWithLetter() {
            var request = new UserCreateRequest("Alice", "Alice", ".Doe", "secret", false, "alice@example.org", false);

            var violations = validator.validate(request);

            assertTrue(violations.stream().anyMatch(v -> "lastName".equals(v.getPropertyPath().toString())));
        }

        @Test
        void acceptsLastName_whenItStartsWithLetterAndUsesAllowedCharacters() {
            var request = new UserCreateRequest("Alice", "Alice", "D-oe.", "secret", false, "alice@example.org", false);

            var violations = validator.validate(request);

            assertTrue(violations.stream().noneMatch(v -> "lastName".equals(v.getPropertyPath().toString())));
        }
    }

    @Nested
    class EmailValidationTests {

        @Test
        void rejectsEmail_whenInvalid() {
            var request = new UserCreateRequest("Alice", "Alice", "Doe", "secret", false, "not-an-email", false);

            var violations = validator.validate(request);

            assertTrue(violations.stream().anyMatch(v -> "email".equals(v.getPropertyPath().toString())));
        }

        @Test
        void acceptsEmail_whenValid() {
            var violations = validator.validate(validRequest());

            assertTrue(violations.stream().noneMatch(v -> "email".equals(v.getPropertyPath().toString())));
        }
    }
}

