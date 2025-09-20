package com.damai.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExaminationSimpleResult {

    /**
     * 节目id
     * */
    private Long programId;

    /**
     * 对比结果
     * */
    private List<ExaminationIdentifierResult> examinationIdentifierResultList;

    
}
