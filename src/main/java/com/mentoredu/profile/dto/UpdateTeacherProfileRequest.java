package com.mentoredu.profile.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTeacherProfileRequest {

    @Size(max = 2000, message = "bioProfessional must not exceed 2000 characters")
    private String bioProfessional;

    @Size(max = 320, message = "universities must not exceed 320 characters")
    private String universities;

    @Size(max = 180, message = "specialty must not exceed 180 characters")
    private String specialty;

    @Size(max = 300, message = "courses must not exceed 300 characters")
    private String courses;

    @Size(max = 420, message = "experience must not exceed 420 characters")
    private String experience;

    @Size(max = 520, message = "summary must not exceed 520 characters")
    private String summary;
}
