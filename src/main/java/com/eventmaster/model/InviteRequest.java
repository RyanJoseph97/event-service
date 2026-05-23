package com.eventmaster.model;

import javax.validation.constraints.NotBlank;

public class InviteRequest {

    @NotBlank
    private String username;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
