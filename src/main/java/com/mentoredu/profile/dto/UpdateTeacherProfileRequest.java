package com.mentoredu.profile.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTeacherProfileRequest {

    @Size(max = 2000, message = "bioProfessional must not exceed 2000 characters")
    private String bioProfessional;
}
