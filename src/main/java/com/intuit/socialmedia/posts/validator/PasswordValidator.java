package com.intuit.socialmedia.posts.validator;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return true; // consider null valid if not required
        }

        // Password validation logic
        return password.length() >= 8 && // Minimum length
                password.chars().anyMatch(Character::isUpperCase) && // At least one uppercase
                password.chars().anyMatch(Character::isLowerCase) && // At least one lowercase
                password.chars().anyMatch(Character::isDigit) && // At least one digit
                password.chars().anyMatch(ch -> "!@#$%^&*()_+[]{}|;:,.<>?/`~".indexOf(ch) >= 0); // At least one special character
    }
}

