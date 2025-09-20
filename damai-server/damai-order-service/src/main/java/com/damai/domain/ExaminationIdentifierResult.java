package com.damai.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExaminationIdentifierResult {

    /**
     * 记录标识
     * */
    private String identifierId;

    /**
     * 用户id
     * */
    private String userId;
    
    /**
     * 记录类型的集合
     * */
    List<ExaminationRecordTypeResult> examinationRecordTypeResultList;
}
