package com.damai.context.model;

import lombok.Data;

@Data
public class WindowMessage {
    private String role;
    private String content;
    private Long timestamp;
}
