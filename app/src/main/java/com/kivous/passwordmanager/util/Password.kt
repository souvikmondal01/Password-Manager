package com.kivous.passwordmanager.util

object Password {
    enum class PasswordStrength {
        EMPTY,
        WEAK,
        MEDIUM,
        STRONG
    }

    fun checkPasswordStrength(password: String): PasswordStrength {
        val minLength = 8
        val minDigitCount = 1
        val minUpperCaseCount = 1
        val minLowerCaseCount = 1
        val minSpecialCharCount = 1

        var strength = PasswordStrength.WEAK

        if (password.isEmpty()) {
            return PasswordStrength.EMPTY
        }

        // Check minimum length
        if (password.length < minLength) {
            return PasswordStrength.WEAK
        }

        // Check for at least one digit
        val digitCount = password.count { it.isDigit() }
        if (digitCount < minDigitCount) {
            return PasswordStrength.WEAK
        }

        // Check for at least one uppercase letter
        val upperCaseCount = password.count { it.isUpperCase() }
        if (upperCaseCount < minUpperCaseCount) {
            return PasswordStrength.WEAK
        }

        // Check for at least one lowercase letter
        val lowerCaseCount = password.count { it.isLowerCase() }
        if (lowerCaseCount < minLowerCaseCount) {
            return PasswordStrength.WEAK
        }

        // Check for at least one special character
        val specialCharCount = password.count { !it.isLetterOrDigit() }
        if (specialCharCount < minSpecialCharCount) {
            return PasswordStrength.WEAK
        }

        // Password meets all requirements, set strength based on length
        strength = if (password.length < 12) {
            PasswordStrength.MEDIUM
        } else {
            PasswordStrength.STRONG
        }

        return strength
    }


    fun generateStrongPassword(length: Int = 12): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9') + listOf(
            '!',
            '@',
            '#',
            '$',
            '%',
            '^',
            '&',
            '*',
            '(',
            ')',
            '-',
            '_',
            '=',
            '+'
        )

        var password = ""
        do {
            password = (1..length)
                .map { allowedChars.random() }
                .joinToString("")
        } while (checkPasswordStrength(password) != PasswordStrength.STRONG)

        return password
    }

}