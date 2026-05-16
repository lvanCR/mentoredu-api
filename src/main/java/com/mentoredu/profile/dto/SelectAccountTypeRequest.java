package com.mentoredu.profile.dto;

import com.mentoredu.profile.model.ProfileType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SelectAccountTypeRequest {

    @NotNull(message = "profileType is required")
    private ProfileType profileType;
}
