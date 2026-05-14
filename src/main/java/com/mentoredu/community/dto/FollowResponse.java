package com.mentoredu.community.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class FollowResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
}
