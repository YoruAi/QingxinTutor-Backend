package com.yoru.qingxintutor.pojo.request;

import com.yoru.qingxintutor.annotation.OptionalNotBlank;
import com.yoru.qingxintutor.pojo.entity.TeacherEntity;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherSearchRequest {
    @OptionalNotBlank(message = "Text cannot be composed of only whitespace characters")
    @Size(min = 1, message = "Text must be longer than 1 character")
    private String text;

    @Min(value = 1, message = "Grade must be between 1 and 9")
    @Max(value = 9, message = "Grade must be between 1 and 9")
    private Integer grade;

    @Min(value = 1, message = "MinAge must be between 1 and 100")
    @Max(value = 100, message = "MinAge must be between 1 and 100")
    private Integer minAge;

    @Min(value = 1, message = "MaxAge must be between 1 and 100")
    @Max(value = 100, message = "MaxAge must be between 1 and 100")
    private Integer maxAge;

    private List<String> subjectNames;

    private TeacherEntity.Gender gender;

    @AssertTrue(message = "Maximum age cannot be less than minimum age")
    public boolean isAgeRangeValid() {
        if (minAge == null || maxAge == null) {
            return true;
        }
        return maxAge >= minAge;
    }
}