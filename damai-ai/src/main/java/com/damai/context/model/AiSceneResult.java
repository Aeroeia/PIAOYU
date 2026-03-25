package com.damai.context.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiSceneResult {
    private AiSceneType scene;
    private Double confidence;
    private String reason;
}
