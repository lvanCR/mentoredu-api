package com.mentoredu.academy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCampusRequest {

    @NotBlank(message = "name is required")
    @Size(max = 120, message = "name must not exceed 120 characters")
    private String name;

    @NotBlank(message = "address is required")
    @Size(max = 180, message = "address must not exceed 180 characters")
    private String address;

    @NotBlank(message = "city is required")
    @Size(max = 80, message = "city must not exceed 80 characters")
    private String city;
}
