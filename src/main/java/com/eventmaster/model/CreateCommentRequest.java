package com.eventmaster.model;

import javax.validation.constraints.NotBlank;

public class CreateCommentRequest {

    @NotBlank(message = "Content must not be blank")
    private String content;

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
