package com.yoru.qingxintutor.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.*;
import java.util.regex.Pattern;

@Documented
@Constraint(validatedBy = ValidUsernameValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Username {
    String message() default "Username invalid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

class ValidUsernameValidator implements ConstraintValidator<Username, String> {
    // 只允许中文 + 字母 + 数字 + 常见安全符号
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[\\w\\u4e00-\\u9fa5\\-.·]{1,50}$");

    @Override
    public boolean isValid(String username, ConstraintValidatorContext context) {
        if (username == null) {
            return true;
        }
        return USERNAME_PATTERN.matcher(username).matches();
    }
}