package com.damai.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExaminationProgramResult {
    
    /**
     * 记录标识的集合
     * */
    private List<ExaminationIdentifierResult> examinationIdentifierResultList;
}
