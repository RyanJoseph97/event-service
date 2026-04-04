package com.eventmaster.model;

import javax.validation.constraints.NotNull;

public class RsvpRequest {

    @NotNull(message = "Status is required")
    private RsvpStatus status;

    public RsvpStatus getStatus() { return status; }
    public void setStatus(RsvpStatus status) { this.status = status; }
}
