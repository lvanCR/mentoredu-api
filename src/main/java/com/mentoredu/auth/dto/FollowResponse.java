package com.mentoredu.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FollowResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
}
