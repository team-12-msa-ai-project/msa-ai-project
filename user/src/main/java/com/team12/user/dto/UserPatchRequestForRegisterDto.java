package com.team12.user.dto;

import com.team12.user.domain.UserRoleEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserPatchRequestForRegisterDto {
    private String username;
    private String name;
    private String slackEmail;
    private UserRoleEnum userRoleEnum;
    private boolean isConfirmed;
}
