package com.sribalajiads.media_app.dto;

import com.sribalajiads.media_app.model.Role;
import lombok.Data;

@Data
public class CreateUserRequest {
    private String username;
    private String password;
    private Role role;
}
