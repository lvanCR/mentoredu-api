package com.mentoredu.profile.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateOrganizationProfileRequest {

    @NotBlank(message = "organizationName is required")
    @Size(max = 120, message = "organizationName must not exceed 120 characters")
    private String organizationName;

    @Size(max = 20, message = "ruc must not exceed 20 characters")
    private String ruc;

    @Size(max = 255, message = "website must not exceed 255 characters")
    private String website;

    @Email(message = "contactEmail must be a valid email address")
    @Size(max = 120, message = "contactEmail must not exceed 120 characters")
    private String contactEmail;
}
