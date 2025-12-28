package com.yoru.qingxintutor.pojo.request;

import com.yoru.qingxintutor.annotation.Phone;
import com.yoru.qingxintutor.pojo.entity.TeacherEntity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherRegisterRequest {
    @Valid
    @NotNull(message = "User information is required")
    private UserRegisterRequest userRegisterRequest;

    @Valid
    @NotNull(message = "Teacher information is required")
    private TeacherInfo teacherInfo;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TeacherInfo {
        @NotBlank(message = "Phone number is required")
        @Phone
        private String phone;

        @NotBlank(message = "Nickname is required")
        @Size(max = 50, message = "Nickname must not exceed 50 characters")
        private String nickname;

        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
        private String name;

        @NotNull(message = "Gender is required")
        private TeacherEntity.Gender gender;

        @NotNull(message = "Birth date is required")
        @Past(message = "Birth date must be in the past")
        private LocalDate birthDate;

        @NotBlank(message = "Address is required")
        @Size(max = 255, message = "Address must not exceed 255 characters")
        private String address;

        @NotBlank(message = "Teaching experience is required")
        @Size(max = 1000, message = "Teaching experience must not exceed 1000 characters")
        private String teachingExperience;

        @NotBlank(message = "Description is required")
        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        private String description;

        @NotNull
        @Min(value = 1, message = "Grade must be between 1 and 9")
        @Max(value = 9, message = "Grade must be between 1 and 9")
        private Integer grade;

        private List<String> subjectNames;
    }
}

